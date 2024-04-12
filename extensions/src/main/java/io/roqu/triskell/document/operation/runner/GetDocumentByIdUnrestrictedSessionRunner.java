package io.roqu.triskell.document.operation.runner;

import lombok.Getter;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;

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
     * Document model.
     */
    private DocumentModel document;


    /**
     * Constructor.
     *
     * @param session session
     * @param id      document identifier
     */
    public GetDocumentByIdUnrestrictedSessionRunner(CoreSession session, String id) {
        super(session);
        this.id = id;
    }


    @Override
    public void run() {
        ElasticSearchService service = Framework.getService(ElasticSearchService.class);

        String query = String.format(QUERY, this.id);
        NxQueryBuilder queryBuilder = new NxQueryBuilder(this.session).nxql(query).limit(2);

        DocumentModelList documents = service.query(queryBuilder);

        if (documents.size() == 1) {
            this.document = documents.get(0);
        }
    }

}
