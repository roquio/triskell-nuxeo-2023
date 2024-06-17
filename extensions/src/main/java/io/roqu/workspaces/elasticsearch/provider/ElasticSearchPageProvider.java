package io.roqu.workspaces.elasticsearch.provider;

import org.apache.commons.collections4.CollectionUtils;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;
import org.nuxeo.ecm.core.api.model.impl.primitives.BlobProperty;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.elasticsearch.api.EsResult;
import org.nuxeo.elasticsearch.io.JsonDocumentModelReader;
import org.nuxeo.elasticsearch.provider.ElasticSearchNxqlPageProvider;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ElasticSearch page provider.
 *
 * @author Cédric Krommenhoek
 * @see CoreQueryDocumentPageProvider
 */
public class ElasticSearchPageProvider extends ElasticSearchNxqlPageProvider {

    /**
     * Page provider name.
     */
    public static final String NAME = "ELASTIC_SEARCH_PAGE_PROVIDER";


    @Override
    public List<DocumentModel> getCurrentPage() {
        if (this.currentPageDocuments == null) {
            this.error = null;
            this.errorMessage = null;

            // ElasticSearch service
            ElasticSearchService elasticSearchService = Framework.getService(ElasticSearchService.class);
            // Core session
            CoreSession session = this.getCoreSession();

            // Query
            if (this.query == null) {
                this.buildQuery(session);
            }
            if (this.query == null) {
                throw new NuxeoException(String.format("Cannot perform null query: check provider '%s'", getName()));
            }

            try {
                // Query builder
                NxQueryBuilder queryBuilder = this.getQueryBuilder(session);
                queryBuilder.nxql(this.query);
                queryBuilder.offset(Math.toIntExact(this.getCurrentPageOffset()));
                queryBuilder.limit(this.getLimit());
                if (this.searchOnAllRepositories()) {
                    queryBuilder.searchOnAllRepositories();
                }
                queryBuilder.useUnrestrictedSession(this.useUnrestrictedSession());
                List<String> highlightFields = getHighlights();
                if (highlightFields != null && !highlightFields.isEmpty()) {
                    queryBuilder.highlight(highlightFields);
                }
                queryBuilder.onlyElasticsearchResponse();

                // ElasticSearch result
                EsResult esResult = elasticSearchService.queryAndAggregate(queryBuilder);
                // ElasticSearch hits
                SearchHits hits = esResult.getElasticsearchResponse().getHits();

                this.currentPageDocuments = Arrays.stream(hits.getHits()).map(this::map).toList();

                if (hits.getTotalHits() != null) {
                    this.setResultsCount(hits.getTotalHits().value);
                }
            } catch (Exception e) {
                this.error = e;
                this.errorMessage = e.getMessage();
            }
        }

        return this.currentPageDocuments;
    }


    /**
     * Map ElasticSearch hit to document model.
     * Forked from {@link JsonDocumentModelReader#getDocumentModel()}
     *
     * @param hit ElasticSearch hit
     * @return document model
     */
    private DocumentModel map(SearchHit hit) {
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
