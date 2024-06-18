package io.roqu.workspaces.elasticsearch.provider;

import io.roqu.workspaces.elasticsearch.service.CustomizedElasticSearchService;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;
import org.nuxeo.elasticsearch.provider.ElasticSearchNxqlPageProvider;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;

import java.util.List;

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
            CustomizedElasticSearchService elasticSearchService = Framework.getService(CustomizedElasticSearchService.class);
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

                // ElasticSearch result
                DocumentModelList documents = elasticSearchService.query(queryBuilder);

                this.currentPageDocuments = documents;

                this.setResultsCount(documents.totalSize());
            } catch (Exception e) {
                this.error = e;
                this.errorMessage = e.getMessage();
            }
        }

        return this.currentPageDocuments;
    }

}
