package io.roqu.workspaces.membermanagement.service;

import io.roqu.workspaces.membermanagement.WorkspaceMemberManagementConstants;
import io.roqu.workspaces.membermanagement.model.WorkspaceGroup;
import io.roqu.workspaces.membermanagement.model.WorkspaceMember;
import io.roqu.workspaces.membermanagement.model.WorkspaceMembers;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.directory.sql.SQLDirectory;
import org.nuxeo.ecm.directory.sql.SQLSession;
import org.nuxeo.ecm.platform.usermanager.NuxeoGroupImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;
import java.util.*;

import static io.roqu.workspaces.membermanagement.WorkspaceMemberManagementConstants.*;

/**
 * Service implementation for user management in a workspace
 *
 * @author Loïc Billon
 */
public class WorkspaceMemberManagementServiceImpl implements WorkspaceMemberManagementService {

    /**
     * Get all members and roles in a workspace
     *
     * @param workspaceId
     * @return
     */
    public WorkspaceMembers getWorkspaceMembers(String workspaceId) {


        WorkspaceMembers workspaceMembers = new WorkspaceMembers();
        workspaceMembers.setWorkspaceId(workspaceId);

        UserManager userManager = Framework.getService(UserManager.class);

        DirectoryService directoryService = Framework.getService(DirectoryService.class);

        SQLDirectory sqlDir = (SQLDirectory) directoryService.getDirectory(USER_TO_WKS_DIR);
        Map<String, Serializable> filters = new HashMap<>();
        filters.put(GROUP_WORKSPACE_ID, workspaceId);
        SQLSession sqlDirSession = sqlDir.getSession();
        DocumentModelList entriesWithJoinedDates = sqlDirSession .query(filters);
        sqlDirSession.close();

        // Get space group (head of all groups)
        NuxeoGroup spaceGroup = userManager.getGroup(workspaceId);
        if(spaceGroup != null) {
            for(String childrenSpaceGroupId : spaceGroup.getMemberGroups()) {

                NuxeoGroup childGroup = userManager.getGroup(childrenSpaceGroupId);
                DocumentModel groupModel = childGroup.getModel();
                // List all other groups
                for(String children : childGroup.getMemberUsers()) {

                    // Add member if he belongs to a security group
                    if(groupModel.getPropertyValue(GROUP_WORKSPACE_GROUP_TYPE).equals(WorkspaceGroup.GroupType.security_group.toString())) {

                        WorkspaceMember member = new WorkspaceMember();

                        DocumentModel userModel = userManager.getUserModel(children);

                        member.setId(userModel.getPropertyValue(USER_NAME).toString());
                        member.setFirstName(userModel.getPropertyValue(USER_FIRST_NAME).toString());
                        member.setLastName(userModel.getPropertyValue(USER_LAST_NAME).toString());

                        member.setRole(WorkspaceGroup.Role.valueOf(groupModel.getPropertyValue(GROUP_ROLE).toString()));

                        for(DocumentModel entry : entriesWithJoinedDates) {
                            if(entry.getPropertyValue(USER_NAME).equals(userModel.getPropertyValue(USER_NAME))) {

                                if(entry.getPropertyValue(USER_TO_WKS_DATE_FIELD) instanceof GregorianCalendar calendar) {

                                    member.setJoinedDate(calendar.getTime());
                                }
                            }
                        }
                        workspaceMembers.getMembers().add(member);
                    }
                }
            }

            return workspaceMembers;

        }
        else {
            throw new NuxeoException("No space group found for workspace " + workspaceId);
        }


    }

    @Override
    public void createWorkspaceGroups(String workspaceId, String owner) {

        UserManager userManager = Framework.getService(UserManager.class);
        NuxeoGroupImpl readers = new WorkspaceGroup(workspaceId, WorkspaceGroup.Role.reader);
        userManager.createGroup(readers.getModel());
        NuxeoGroupImpl writers = new WorkspaceGroup(workspaceId, WorkspaceGroup.Role.writer);
        userManager.createGroup(writers.getModel());
        NuxeoGroupImpl admins = new WorkspaceGroup(workspaceId, WorkspaceGroup.Role.administrator);
        admins.setMemberUsers(List.of(owner));
        userManager.createGroup(admins.getModel());

        DirectoryService directoryService = Framework.getService(DirectoryService.class);
        SQLDirectory sqlDir = (SQLDirectory) directoryService.getDirectory(USER_TO_WKS_DIR);
        SQLSession sqlDirSession = sqlDir.getSession();
        Map<String, Object> ownerJoined = new HashMap<>();
        ownerJoined.put(USER_NAME, owner);
        ownerJoined.put(GROUP_WORKSPACE_ID, workspaceId);
        ownerJoined.put(WorkspaceMemberManagementConstants.USER_TO_WKS_DATE_FIELD, Calendar.getInstance());
        sqlDirSession.createEntry(ownerJoined);
        sqlDirSession.close();

        NuxeoGroupImpl members = new WorkspaceGroup(workspaceId);
        members.setMemberGroups(List.of(readers.getName(), writers.getName(), admins.getName()));
        userManager.createGroup(members.getModel());

    }
}
