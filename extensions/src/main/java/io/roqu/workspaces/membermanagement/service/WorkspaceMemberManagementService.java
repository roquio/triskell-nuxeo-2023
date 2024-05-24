package io.roqu.workspaces.membermanagement.service;

import io.roqu.workspaces.membermanagement.model.WorkspaceMembers;

/**
 * Service implementation for user management in a workspace
 * @author Loïc Billon
 */
public interface WorkspaceMemberManagementService {

    /**
     * Get all members and roles on this workspace
     * @param workspaceId
     * @return
     */
    WorkspaceMembers getWorkspaceMembers(String workspaceId);

    /**
     * Create workspace groups
     *
     * @param workspaceId
     * @param owner
     */
    void createWorkspaceGroups(String workspaceId, String owner);
}
