package io.roqu.workspaces.elasticsearch.service;

import io.roqu.workspaces.elasticsearch.fetcher.CustomizedElasticSearchFetcher;
import lombok.RequiredArgsConstructor;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.elasticsearch.api.EsResult;
import org.nuxeo.elasticsearch.api.EsScrollResult;
import org.nuxeo.elasticsearch.fetcher.Fetcher;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;
import org.opensearch.action.search.SearchResponse;

/**
 * Customized ElasticSearch service implementation.
 *
 * @author Cédric Krommenhoek
 */
@RequiredArgsConstructor
public class CustomizedElasticSearchServiceImpl implements CustomizedElasticSearchService {

    @Override
    public DocumentModelList query(NxQueryBuilder queryBuilder) {
        return this.queryAndAggregate(queryBuilder).getDocuments();
    }


    @Override
    public EsResult queryAndAggregate(NxQueryBuilder queryBuilder) {
        // ElasticSearch service
        ElasticSearchService elasticSearchService = Framework.getService(ElasticSearchService.class);

        boolean returnsDocuments = queryBuilder.returnsDocuments();

        queryBuilder.onlyElasticsearchResponse();

        EsResult esResult = elasticSearchService.queryAndAggregate(queryBuilder);

        if (returnsDocuments) {
            DocumentModelListImpl docs = this.getDocumentModels(queryBuilder, esResult.getElasticsearchResponse());
            esResult = new EsResult(docs, esResult.getAggregates(), esResult.getElasticsearchResponse());
        }

        return esResult;
    }


    private DocumentModelListImpl getDocumentModels(NxQueryBuilder queryBuilder, SearchResponse response) {
        // Total size
        long totalSize;
        if (response.getHits().getTotalHits() == null) {
            totalSize = 0L;
        } else {
            totalSize = response.getHits().getTotalHits().value;
        }

        // Documents
        DocumentModelListImpl documents;
        if (response.getHits().getHits().length == 0) {
            documents = new DocumentModelListImpl(0);
            documents.setTotalSize(totalSize);
        } else {
            Fetcher fetcher = new CustomizedElasticSearchFetcher(queryBuilder.getSession(), response, null);
            documents = fetcher.fetchDocuments();
            documents.setTotalSize(totalSize);
        }

        return documents;
    }


    @Override
    public EsScrollResult scroll(NxQueryBuilder queryBuilder, long keepAlive) {
        throw new RuntimeException("Not implemented");
    }


    @Override
    public EsScrollResult scroll(EsScrollResult scrollResult) {
        throw new RuntimeException("Not implemented");
    }


    @Override
    public void clearScroll(EsScrollResult scrollResult) {
        throw new RuntimeException("Not implemented");
    }

}
