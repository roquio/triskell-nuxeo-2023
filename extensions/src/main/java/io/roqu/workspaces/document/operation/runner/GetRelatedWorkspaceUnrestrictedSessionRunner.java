package io.roqu.workspaces.document.operation.runner;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.nuxeo.ecm.core.api.*;

import java.util.List;
import java.util.Set;

/**
 * Get related workspace unrestricted session runner.
 *
 * @author Cédric Krommenhoek
 * @see UnrestrictedSessionRunner
 */
@Getter
public class GetRelatedWorkspaceUnrestrictedSessionRunner extends UnrestrictedSessionRunner {

    /**
     * Workspace types.
     */
    private static final Set<String> TYPES = Set.of("Workspace", "Room");


    /**
     * Document path.
     */
    private final String path;

    /**
     * Related workspace document.
     */
    private DocumentModel workspace;


    /**
     * Constructor.
     *
     * @param session session
     * @param path    document path
     */
    public GetRelatedWorkspaceUnrestrictedSessionRunner(CoreSession session, String path) {
        super(session);
        this.path = path;
    }


    @Override
    public void run() {
        DocumentRef documentRef = new PathRef(this.path);

        List<DocumentModel> parentDocuments = this.session.getParentDocuments(documentRef);
        if (CollectionUtils.isNotEmpty(parentDocuments)) {
            this.workspace = parentDocuments.stream().filter(document -> TYPES.contains(document.getType())).findFirst().orElse(null);
        }
    }

}
