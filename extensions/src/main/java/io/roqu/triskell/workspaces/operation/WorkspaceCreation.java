package io.roqu.triskell.workspaces.operation;

import io.roqu.triskell.TriskellConstants;
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
import org.nuxeo.ecm.platform.usermanager.NuxeoGroupImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

import java.util.List;


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

        DocumentModel workspace = session.createDocumentModel(workspaceRoot.getPathAsString(), title, "Workspace");
        workspace.setPropertyValue("dc:title", title);
        workspace = session.createDocument(workspace);

        String businessId = workspace.getPropertyValue(TriskellConstants.TK_ID_FIELD).toString();

        UserManager userManager = Framework.getService(UserManager.class);
        NuxeoGroupImpl readers = new NuxeoGroupImpl(businessId+"_readers", "Readers of "+businessId);
        userManager.createGroup(readers.getModel());
        NuxeoGroupImpl writers = new NuxeoGroupImpl(businessId+"_writers", "Writers of "+businessId);
        userManager.createGroup(writers.getModel());
        NuxeoGroupImpl admins = new NuxeoGroupImpl(businessId+"_admins", "Admins of "+businessId);
        admins.setMemberUsers(List.of(owner));
        userManager.createGroup(admins.getModel());

        NuxeoGroupImpl members = new NuxeoGroupImpl(businessId+"_members", "Members of "+businessId);
        members.setMemberGroups(List.of(readers.getName(), writers.getName(), admins.getName()));
        userManager.createGroup(members.getModel());

        ACP acp = new ACPImpl();


        acp.addACE("local", new ACE.ACEBuilder(readers.getName(),"Read").isGranted(true).build());
        acp.addACE("local", new ACE.ACEBuilder(writers.getName(),"ReadWrite").isGranted(true).build());
        acp.addACE("local", new ACE.ACEBuilder(admins.getName(),"Everything").isGranted(true).build());

        session.setACP(workspace.getRef(), acp, true);

        return workspace;
    }


}
