package io.roqu.triskell.document.operation;

import io.roqu.triskell.document.operation.runner.GetDocumentByIdUnrestrictedSessionRunner;
import io.roqu.triskell.document.operation.runner.GetRelatedWorkspaceUnrestrictedSessionRunner;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

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
     * Run operation.
     *
     * @return operation result
     */
    @OperationMethod
    public Blob run() throws OperationException {
        // Document reference
        DocumentRef documentRef;
        if (StringUtils.isEmpty(this.uuid)) {
            // Document path
            String path;
            if (StringUtils.isEmpty(this.path)) {
                // Get document with unrestricted rights
                DocumentModel unrestrictedDocument = this.getUnrestrictedDocument(this.id);
                if (unrestrictedDocument == null) {
                    throw new DocumentNotFoundException(this.id);
                }

                path = unrestrictedDocument.getPathAsString();
            } else {
                path = this.path;
            }

            documentRef = new PathRef(path);
        } else {
            documentRef = new IdRef(this.uuid);
        }

        // Document
        DocumentModel document = this.session.getDocument(documentRef);
        // Related workspace
        DocumentModel workspace = this.getRelatedWorkspace(document.getPathAsString());

        // JSON object
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("id", document.getPropertyValue("tk:id"));
        jsonObject.accumulate("path", document.getPathAsString());
        jsonObject.accumulate("uuid", document.getId());
        if (workspace != null) {
            jsonObject.accumulate("workspaceId", workspace.getPropertyValue("tk:id"));
            jsonObject.accumulate("workspacePath", workspace.getPathAsString());
        }

        return new StringBlob(jsonObject.toString(), "application/json");
    }


    /**
     * Get unrestricted document.
     *
     * @param id document identifier
     * @return document
     */
    private DocumentModel getUnrestrictedDocument(String id) {
        GetDocumentByIdUnrestrictedSessionRunner runner = new GetDocumentByIdUnrestrictedSessionRunner(this.session, id);
        runner.runUnrestricted();

        return runner.getDocument();
    }


    /**
     * Get related workspace document.
     *
     * @param path document path
     * @return document
     */
    private DocumentModel getRelatedWorkspace(String path) {
        GetRelatedWorkspaceUnrestrictedSessionRunner runner = new GetRelatedWorkspaceUnrestrictedSessionRunner(this.session, path);
        runner.runUnrestricted();

        DocumentModel workspace;
        if (runner.getWorkspace() == null) {
            // Not found
            workspace = null;
        } else if (this.session.hasPermission(runner.getWorkspace().getRef(), SecurityConstants.READ)) {
            workspace = runner.getWorkspace();
        } else {
            // Forbidden
            workspace = null;
        }

        return workspace;
    }

}
