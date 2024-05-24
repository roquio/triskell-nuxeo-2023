package io.roqu.workspaces.membermanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Loïc Billon
 */
@Data
public class WorkspaceMembers {

    private String workspaceId;

    private List<WorkspaceMember> members = new ArrayList<>();


    @JsonProperty("entity-type")
    private String entityType = this.getClass().getSimpleName();

}
