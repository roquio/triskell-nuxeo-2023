package io.roqu.triskell.elasticsearch.operation;

import lombok.NoArgsConstructor;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.elasticsearch.api.EsResult;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;

/**
 * ElasticSearch query operation.
 *
 * @author Cédric Krommenhoek
 */
@Operation(id = ElasticSearchQuery.ID, category = Constants.CAT_FETCH, label = "ElasticSearch query", description = "Perform a query on ElasticSearch, instead of on the repository.", aliases = {"Document.ElasticSearchQuery", "Document.QueryES"})
@NoArgsConstructor
public class ElasticSearchQuery {

    /**
     * Operation identifier.
     */
    public static final String ID = "Repository.ElasticSearchQuery";


    /**
     * Session.
     */
    @Context
    protected CoreSession session;

    /**
     * Query.
     */
    @Param(name = "query", description = "The query to perform.")
    protected String query;


    /**
     * Run operation.
     *
     * @return operation result
     */
    @OperationMethod
    public DocumentModelList run() throws OperationException {
        ElasticSearchService service = Framework.getService(ElasticSearchService.class);
        NxQueryBuilder queryBuilder = new NxQueryBuilder(this.session).nxql(this.query).limit(10000);
        EsResult esResult = service.queryAndAggregate(queryBuilder);

        return esResult.getDocuments();
    }

}
