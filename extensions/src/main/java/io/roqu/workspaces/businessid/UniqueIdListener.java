package io.roqu.workspaces.businessid;

import io.roqu.workspaces.WorkspacesConstants;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;


/**
 * Listener called in creation to set a short unique identifier.
 *
 * @author Loïc Billon
 */
public class UniqueIdListener implements EventListener {

    @Override
    public void handleEvent(Event event) {

        if(event.getContext() instanceof DocumentEventContext docCtx) {
            DocumentModel sourceDocument = docCtx.getSourceDocument();
            if(sourceDocument.hasSchema(WorkspacesConstants.NAVIGATION_SCHEMA)) {

                try {
                    BusinessIdRunner runner = new BusinessIdRunner(event.getContext().getCoreSession());
                    runner.runUnrestricted();
                    String newId = runner.getRandomId();

                    sourceDocument.setPropertyValue(WorkspacesConstants.ID_FIELD, newId);
                }
                catch (NuxeoException e) {
                    event.markBubbleException();
                    throw e;

                }


            }
        }

    }


}
