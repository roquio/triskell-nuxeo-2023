package io.roqu.triskell.document.operation;

import io.roqu.triskell.TriskellConstants;
import io.roqu.triskell.document.operation.runner.GetDocumentByIdUnrestrictedSessionRunner;
import io.roqu.triskell.document.operation.runner.GetRelatedWorkspaceUnrestrictedSessionRunner;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;

/**
 * Get document context operation.
 *
 * @author Cédric Krommenhoek
 */
@Operation(id = GetDocumentContext.ID, category = Constants.CAT_DOCUMENT, label = "Get document context")
public class GetDocumentContext {

    /**
     * Operation identifier.
     */
    public final static String ID = "Document.GetContext";


    /**
     * Session.
     */
    @Context
    protected CoreSession session;

    /**
     * Document identifier.
     */
    @Param(name = "id", description = "Document identifier", required = false)
    protected String id;

    /**
     * Document path.
     */
    @Param(name = "path", description = "Document path", required = false)
    protected String path;

    /**
     * Document technical identifier.
     */
    @Param(name = "uuid", description = "Document technical identifier", required = false)
    protected String uuid;

    /**
     * Document repository name.
     */
    @Param(name = "repository", description = "Document repository name", required = false)
    protected String repository;

    /**
     * Search on all repositories.
     */
    @Param(name = "searchOnAllRepositories", description = "Search on all repositories", required = false)
    protected Boolean searchOnAllRepositories;


    /**
     * Run operation.
     *
     * @return operation result
     */
    @OperationMethod
    public Blob run() {
        // Session
        CoreSession session;
        if (StringUtils.isEmpty(this.repository) || StringUtils.equals(this.repository, this.session.getRepositoryName())) {
            session = this.session;
        } else {
            session = SessionFactory.getSession(this.repository);
        }

        // Document reference
        DocumentRef documentRef;
        if (StringUtils.isEmpty(this.uuid)) {
            // Document path
            String path;
            if (StringUtils.isEmpty(this.path)) {
                // Get document with unrestricted rights
                Pair<String, DocumentModel> unrestrictedDocument = this.getUnrestrictedDocument(session, this.id, BooleanUtils.isTrue(this.searchOnAllRepositories));
                if ((unrestrictedDocument == null) || (unrestrictedDocument.getRight() == null)) {
                    throw new DocumentNotFoundException(this.id);
                } else if (StringUtils.isNotEmpty(unrestrictedDocument.getLeft()) && !StringUtils.equals(unrestrictedDocument.getLeft(), session.getRepositoryName())) {
                    // Update session
                    session = SessionFactory.getSession(unrestrictedDocument.getLeft());
                }

                path = unrestrictedDocument.getRight().getPathAsString();
            } else {
                path = this.path;
            }

            documentRef = new PathRef(path);
        } else {
            documentRef = new IdRef(this.uuid);
        }

        // Document
        DocumentModel document = session.getDocument(documentRef);
        // Related workspace
        DocumentModel workspace = this.getRelatedWorkspace(session, document.getPathAsString());

        // JSON object
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("repository", document.getRepositoryName());
        jsonObject.accumulate("id", document.getPropertyValue(TriskellConstants.TK_ID_FIELD));
        jsonObject.accumulate("path", document.getPathAsString());
        jsonObject.accumulate("uuid", document.getId());
        jsonObject.accumulate("type", document.getType());
        jsonObject.accumulate("title", document.getTitle());
        if (workspace != null) {
            jsonObject.accumulate("workspaceId", workspace.getPropertyValue(TriskellConstants.TK_ID_FIELD));
            jsonObject.accumulate("workspacePath", workspace.getPathAsString());
        }

        return new StringBlob(jsonObject.toString(), "application/json");
    }


    /**
     * Get unrestricted document.
     *
     * @param session session
     * @param id      document identifier
     * @return document
     */
    private Pair<String, DocumentModel> getUnrestrictedDocument(CoreSession session, String id, boolean searchOnAllRepositories) {
        GetDocumentByIdUnrestrictedSessionRunner runner = new GetDocumentByIdUnrestrictedSessionRunner(session, id, searchOnAllRepositories);
        runner.runUnrestricted();

        Pair<String, DocumentModel> result;
        if (runner.getDocument() == null) {
            result = null;
        } else {
            result = Pair.of(runner.getRepository(), runner.getDocument());
        }

        return result;
    }


    /**
     * Get related workspace document.
     *
     * @param session session
     * @param path    document path
     * @return document
     */
    private DocumentModel getRelatedWorkspace(CoreSession session, String path) {
        GetRelatedWorkspaceUnrestrictedSessionRunner runner = new GetRelatedWorkspaceUnrestrictedSessionRunner(session, path);
        runner.runUnrestricted();

        DocumentModel workspace;
        if (runner.getWorkspace() == null) {
            // Not found
            workspace = null;
        } else if (session.hasPermission(runner.getWorkspace().getRef(), SecurityConstants.READ)) {
            workspace = runner.getWorkspace();
        } else {
            // Forbidden
            workspace = null;
        }

        return workspace;
    }

}
