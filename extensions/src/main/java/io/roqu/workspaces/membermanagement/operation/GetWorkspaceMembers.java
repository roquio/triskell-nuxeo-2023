package io.roqu.workspaces.membermanagement.operation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.roqu.workspaces.membermanagement.service.WorkspaceMemberManagementService;
import io.roqu.workspaces.membermanagement.model.WorkspaceMembers;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.runtime.api.Framework;

/**
 * Get all members and roles on this workspace
 *
 * @author Loïc Billon
 */
@Operation(id = GetWorkspaceMembers.ID, category = Constants.CAT_BUSINESS, label = "Get workspace members", description = "Get all members and roles on this workspace")
public class GetWorkspaceMembers {


    /**
     * Operation identifier.
     */
    public static final String ID = "Repository.GetWorkspaceMembers";

    /**
     * Session.
     */
    @Context
    protected CoreSession session;


    /**
     * Document identifier.
     */
    @Param(name = "id", description = "Workspace identifier", required = true)
    protected String id;

    /**
     * Run operation.
     *
     * @return operation result
     */
    @OperationMethod
    public StringBlob run() throws JsonProcessingException {


        // TODO valider les habilitations

        WorkspaceMemberManagementService service = Framework.getService(WorkspaceMemberManagementService.class);
        WorkspaceMembers workspaceMembers = service.getWorkspaceMembers(id);

        ObjectMapper objectMapper = new ObjectMapper();
        return new StringBlob( objectMapper.writeValueAsString(workspaceMembers) , "application/json");


    }

}
