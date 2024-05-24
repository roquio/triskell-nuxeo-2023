package io.roqu.workspaces.workspaces.operation;

import io.roqu.workspaces.WorkspacesConstants;
import io.roqu.workspaces.membermanagement.model.WorkspaceGroup;
import io.roqu.workspaces.membermanagement.service.WorkspaceMemberManagementService;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.runtime.api.Framework;


/**
 * Creation of workspace
 * - initialisation of the document
 * - Initialisation of security and members groups
 * - Initialisation of ACLs
 *
 * @author Loïc Billon
 */
@Operation(id = WorkspaceCreation.ID, category = Constants.CAT_BUSINESS, label = "Workspace creation", description = "Create a workspace, define groups, and rolee")
public class WorkspaceCreation {

    /**
     * Operation identifier.
     */
    public static final String ID = "Repository.WorkspaceCreation";

    /**
     * Session.
     */
    @Context
    protected CoreSession session;

    /**
     * Title.
     */
    @Param(name = "title", description = "Workspace title")
    protected String title;

    /**
     * Owner.
     */
    @Param(name = "owner", description = "Owner of the workspace")
    protected String owner;

    @OperationMethod
    public DocumentModel run(DocumentModel parent) {

        DocumentModel workspaceRoot = session.getDocument(parent.getRef());

        // Create workspace document
        DocumentModel workspace = session.createDocumentModel(workspaceRoot.getPathAsString(), title, "Workspace");
        workspace.setPropertyValue("dc:title", title);
        workspace = session.createDocument(workspace);

        String workspaceId = workspace.getPropertyValue(WorkspacesConstants.ID_FIELD).toString();

        // Create groups
        WorkspaceMemberManagementService service = Framework.getService(WorkspaceMemberManagementService.class);
        service.createWorkspaceGroups(workspaceId, owner);

        // Create ACLs
        ACP acp = new ACPImpl();


        acp.addACE("local", new ACE.ACEBuilder(WorkspaceGroup.Role.reader.getAclName(workspaceId),"Read").isGranted(true).build());
        acp.addACE("local", new ACE.ACEBuilder(WorkspaceGroup.Role.writer.getAclName(workspaceId),"ReadWrite").isGranted(true).build());
        acp.addACE("local", new ACE.ACEBuilder(WorkspaceGroup.Role.administrator.getAclName(workspaceId),"Everything").isGranted(true).build());

        session.setACP(workspace.getRef(), acp, true);

        return workspace;
    }


}
