package io.roqu.workspaces.membermanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Representation of a workspace member
 * @author Loïc Billon
 */
@Data
public class WorkspaceMember {

    private String id;

    private String firstName;

    private String lastName;

    /**
     * Workspace role.
     */
    @JsonProperty
    private WorkspaceGroup.Role role = WorkspaceGroup.Role.reader;
    /**
     * Local groups.
     */
    @JsonProperty
    private List<WorkspaceGroup> localGroups = new ArrayList<>();


    @JsonProperty
    private Date joinedDate;

}
