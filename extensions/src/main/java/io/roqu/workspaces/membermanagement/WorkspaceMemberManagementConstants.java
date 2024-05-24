package io.roqu.workspaces.membermanagement;

/**
 * useful constants on member management
 * @author Loïc Billon
 */
public interface WorkspaceMemberManagementConstants {

    // User mapping
    public static final String USER_NAME = "username";
    public static final String USER_FIRST_NAME = "firstName";
    public static final String USER_LAST_NAME = "lastName";

    // Group mapping
    public static final String GROUP_ROLE = "workspaceRole";
    public static final String GROUP_WORKSPACE_GROUP_TYPE = "workspaceGroupType";
    public static final String GROUP_WORKSPACE_ID = "workspaceId";

    // User to workspace table mapping
    public static final String USER_TO_WKS_DATE_FIELD = "joinedDate";
    public static final String USER_TO_WKS_DIR = "user2workspace";

}
