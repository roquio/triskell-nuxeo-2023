package io.roqu.workspaces.elasticsearch.operation;

import lombok.NoArgsConstructor;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.elasticsearch.api.ElasticSearchAdmin;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Cédric Krommenhoek
 */
@Operation(id = ReindexElasticSearch.ID, category = "Administration", label = "Reindex ElasticSearch")
@NoArgsConstructor
public class ReindexElasticSearch {

    /**
     * Operation identifier.
     */
    public static final String ID = "Administration.ReindexElasticSearch";


    /**
     * Session.
     */
    @Context
    protected CoreSession session;


    /**
     * Run operation.
     *
     * @return operation result
     */
    @OperationMethod
    public Object run() throws OperationException {
        ElasticSearchAdmin elasticSearchAdmin = Framework.getService(ElasticSearchAdmin.class);

        elasticSearchAdmin.refresh();

        return null;
    }

}
