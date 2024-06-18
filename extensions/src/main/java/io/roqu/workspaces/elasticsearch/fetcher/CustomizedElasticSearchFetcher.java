package io.roqu.workspaces.elasticsearch.fetcher;

import org.apache.commons.collections4.CollectionUtils;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;
import org.nuxeo.ecm.core.api.model.impl.primitives.BlobProperty;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.elasticsearch.fetcher.Fetcher;
import org.nuxeo.runtime.api.Framework;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.common.text.Text;
import org.opensearch.search.SearchHit;
import org.opensearch.search.fetch.subphase.highlight.HighlightField;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Customized ElasticSearch fetcher.
 *
 * @author Cédric Krommenhoek
 * @see Fetcher
 */
public class CustomizedElasticSearchFetcher extends Fetcher {

    /**
     * Constructor.
     *
     * @param session   core session
     * @param response  search response
     * @param repoNames repository names
     */
    public CustomizedElasticSearchFetcher(CoreSession session, SearchResponse response, Map<String, String> repoNames) {
        super(session, response, repoNames);
    }


    @Override
    public DocumentModelListImpl fetchDocuments() {
        DocumentModelListImpl documents = new DocumentModelListImpl(getResponse().getHits().getHits().length);

        for (SearchHit hit : getResponse().getHits()) {
            DocumentModel document = this.getDocumentModel(hit);

            // Add highlight if it exists
            Map<String, HighlightField> esHighlights = hit.getHighlightFields();
            if (!esHighlights.isEmpty()) {
                Map<String, List<String>> fields = new HashMap<>();
                for (Map.Entry<String, HighlightField> entry : esHighlights.entrySet()) {
                    String field = entry.getKey();
                    List<String> list = new ArrayList<>();
                    for (Text fragment : entry.getValue().getFragments()) {
                        list.add(fragment.toString());
                    }
                    fields.put(field, list);
                }
                document.putContextData(PageProvider.HIGHLIGHT_CTX_DATA, (Serializable) fields);
            }

            documents.add(document);
        }

        return documents;
    }


    private DocumentModel getDocumentModel(SearchHit hit) {
        Map<String, Object> source = hit.getSourceAsMap();

        String type = (String) source.get("ecm:primaryType");
        List<?> mixinTypes = (List<?>) source.get("ecm:mixinType");
        String id = (String) source.get("ecm:uuid");
        String path = (String) source.get("ecm:path");
        String parentId = (String) source.get("ecm:parentId");
        String repositoryName = (String) source.get("ecm:repository");
        boolean isProxy = Boolean.TRUE.equals(source.get("ecm:isProxy"));
        boolean isVersion = Boolean.TRUE.equals(source.get("ecm:isVersion"));

        SchemaManager schemaManager = Framework.getService(SchemaManager.class);
        DocumentType docType = schemaManager.getDocumentType(type);

        Set<String> facets = mixinTypes == null ? Collections.emptySet() : mixinTypes.stream().map(item -> (String) item).filter(item -> !FacetNames.IMMUTABLE.equals(item)).filter(item -> !CollectionUtils.containsAny(docType.getFacets(), item)).collect(Collectors.toSet());

        Path pathObj = path == null ? null : new Path(path);
        DocumentRef docRef = new IdRef(id);
        DocumentRef parentRef = parentId == null ? null : new IdRef(parentId);

        DocumentModelImpl document = new DocumentModelImpl(type, id, pathObj, docRef, parentRef, null, facets, null, isProxy, null, repositoryName, null);
        document.setIsVersion(isVersion);

        for (String schemaName : document.getSchemas()) { // all schemas including from facets
            Schema schema = schemaManager.getSchema(schemaName);
            document.addDataModel(DocumentModelFactory.createDataModel(null, schema));
        }

        for (String xpath : source.keySet()) {
            String schema = xpath.split(":")[0];
            Object value = source.get(xpath);
            if (value instanceof Serializable propertyValue) {
                if ("ecm".equals(schema)) {
                    if ("ecm:currentLifeCycleState".equals(xpath)) {
                        document.prefetchCurrentLifecycleState((String) value);
                    }
                    // Ignore other ecm:* properties
                } else if (document.getProperty(xpath) instanceof BlobProperty blobProperty) {
                    MapProperty mapProperty = new MapProperty(blobProperty.getParent(), blobProperty.getField());
                    mapProperty.init(propertyValue);

                    document.setPropertyObject(mapProperty);
                } else {
                    document.setPropertyValue(xpath, propertyValue);
                }
            }
        }

        document.setIsImmutable(true);

        return document;
    }

}
