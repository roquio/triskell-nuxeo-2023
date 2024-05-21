package io.roqu.workspaces.workspaces.operation;

import lombok.Getter;
import org.nuxeo.ecm.platform.usermanager.NuxeoGroupImpl;

/**
 * Implementation of workspace groups with role and reference to a workspace document
 *
 * @author Loïc Billon
 */
@Getter
public class WorkspaceGroup extends NuxeoGroupImpl  {

    private final String GROUP_WORKSPACE_ID_FIELD = "workspaceId";
    private final String GROUP_WORKSPACE_ROLE_FIELD = "workspaceRole";
    private final String GROUP_WORKSPACE_GROUP_TYPE_FIELD = "workspaceGroupType";

    public enum Role {
        reader,
        writer,
        administrator
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

        model.setProperty(config.schemaName, GROUP_WORKSPACE_ID_FIELD, workspaceId);
        model.setProperty(config.schemaName, GROUP_WORKSPACE_GROUP_TYPE_FIELD, groupType.name());
    }

    public WorkspaceGroup(String workspaceId, Role role) {

        super(workspaceId.concat("_").concat(role.name()));
        this.workspaceId = workspaceId;
        this.role = role;
        this.groupType = GroupType.security_group;

        model.setProperty(config.schemaName, GROUP_WORKSPACE_ID_FIELD, workspaceId);
        model.setProperty(config.schemaName, GROUP_WORKSPACE_GROUP_TYPE_FIELD, groupType.name());
        model.setProperty(config.schemaName, GROUP_WORKSPACE_ROLE_FIELD, role.name());

    }

}
