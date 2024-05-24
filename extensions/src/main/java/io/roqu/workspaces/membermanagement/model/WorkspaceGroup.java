package io.roqu.workspaces.membermanagement.model;

import lombok.Getter;
import org.nuxeo.ecm.platform.usermanager.NuxeoGroupImpl;

import static io.roqu.workspaces.membermanagement.WorkspaceMemberManagementConstants.*;

/**
 * Implementation of workspace groups with role and reference to a workspace document
 *
 * @author Loïc Billon
 */
@Getter
public class WorkspaceGroup extends NuxeoGroupImpl  {


    public enum Role {
        reader,
        writer,
        administrator;

        public String getAclName(String workspaceId) {
            return workspaceId.concat("_").concat(this.name());
        }
    }

    public enum GroupType {
        space_group,
        security_group,
        local_group
    }


    private Role role;

    private final GroupType groupType;

    private String workspaceId;


    public WorkspaceGroup(String workspaceId) {

        super(workspaceId);
        this.workspaceId = workspaceId;
        this.groupType = GroupType.space_group;

        model.setProperty(config.schemaName, GROUP_WORKSPACE_ID, workspaceId);
        model.setProperty(config.schemaName, GROUP_WORKSPACE_GROUP_TYPE, groupType.name());
    }

    public WorkspaceGroup(String workspaceId, Role role) {

        super(role.getAclName(workspaceId));
        this.workspaceId = workspaceId;
        this.role = role;
        this.groupType = GroupType.security_group;

        model.setProperty(config.schemaName, GROUP_WORKSPACE_ID, workspaceId);
        model.setProperty(config.schemaName, GROUP_WORKSPACE_GROUP_TYPE, groupType.name());
        model.setProperty(config.schemaName, GROUP_ROLE, role.name());

    }

}
