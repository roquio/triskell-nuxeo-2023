package io.roqu.triskell.document.operation.runner;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.elasticsearch.api.EsResult;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;

/**
 * Get document by identifier unrestricted session runner.
 *
 * @author Cédric Krommenhoek
 * @see UnrestrictedSessionRunner
 */
@Getter
public class GetDocumentByIdUnrestrictedSessionRunner extends UnrestrictedSessionRunner {

    /**
     * Query
     */
    private static final String QUERY = "SELECT * FROM Document WHERE tk:id = '%s' AND ecm:isVersion = 0 AND ecm:isProxy = 0";


    /**
     * Document identifier.
     */
    private final String id;

    /**
     * Search on all repositories.
     */
    private final boolean searchOnAllRepositories;


    /**
     * Document repository
     */
    private String repository;

    /**
     * Document model.
     */
    private DocumentModel document;


    /**
     * Constructor.
     *
     * @param session                 session
     * @param id                      document identifier
     * @param searchOnAllRepositories search on all repositories
     */
    public GetDocumentByIdUnrestrictedSessionRunner(CoreSession session, String id, boolean searchOnAllRepositories) {
        super(session);
        this.id = id;
        this.searchOnAllRepositories = searchOnAllRepositories;
    }


    @Override
    public void run() {
        ElasticSearchService service = Framework.getService(ElasticSearchService.class);

        // Query
        String query = String.format(QUERY, this.id);
        NxQueryBuilder queryBuilder = new NxQueryBuilder(this.session).nxql(query).limit(2);
        if (this.searchOnAllRepositories) {
            queryBuilder.searchOnAllRepositories();
        }

        // Documents
        DocumentModelList documents;
        if (this.searchOnAllRepositories) {
            // ElasticSearch result
            EsResult result = service.queryAndAggregate(queryBuilder);

            // ElasticSearch hits
            SearchHits hits = result.getElasticsearchResponse().getHits();

            // First ElasticSearch hit
            SearchHit hit = ArrayUtils.get(hits.getHits(), 0);
            if ((hit != null) && (hit.getSourceAsMap().get("ecm:repository") instanceof String hitRepository)) {
                this.repository = hitRepository;
            } else {
                this.repository = this.session.getRepositoryName();
            }

            documents = result.getDocuments();
        } else {
            documents = service.query(queryBuilder);
        }

        if (documents.size() == 1) {
            this.document = documents.get(0);
        }
    }

}
