package io.roqu.workspaces.document.operation;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

import java.util.List;

/**
 * Get document user permissions operation.
 *
 * @author Cédric Krommenhoek
 */
@Operation(id = GetDocumentUserPermissions.ID, category = Constants.CAT_DOCUMENT, label = "Get document user permissions")
public class GetDocumentUserPermissions {

    /**
     * Operation identifier.
     */
    public final static String ID = "Document.GetUserPermissions";


    /**
     * Session.
     */
    @Context
    protected CoreSession session;


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
    public Blob run() {
        // Document reference
        DocumentRef documentRef;
        if (StringUtils.isEmpty(this.uuid)) {
            documentRef = new PathRef(this.path);
        } else {
            documentRef = new IdRef(this.uuid);
        }

        // Permissions
        List<String> permissions = List.of(SecurityConstants.EVERYTHING, SecurityConstants.READ_WRITE, SecurityConstants.REMOVE);

        // JSON object
        JSONObject jsonObject = new JSONObject();
        permissions.forEach(permission -> jsonObject.accumulate(permission, this.session.hasPermission(documentRef, permission)));

        return new StringBlob(jsonObject.toString(), "application/json");
    }

}
