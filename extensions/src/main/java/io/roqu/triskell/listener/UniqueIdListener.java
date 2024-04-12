package io.roqu.triskell.listener;

import org.apache.commons.lang3.RandomStringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
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
            if(sourceDocument.hasSchema("triskell")) {

                String newTskId = createUID();

                // TODO check if not exists

                sourceDocument.setPropertyValue("tsk:id", newTskId);

            }
        }

    }



    private String createUID() {

        return RandomStringUtils.random(8, true, true);

    }

}
