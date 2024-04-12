package io.roqu.triskell.listener;

import io.roqu.triskell.Constants;
import io.roqu.triskell.businessid.BusinessIdService;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;


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
            if(sourceDocument.hasSchema(Constants.TK_SCHEMA)) {

                try {
                    String newId = Framework.getService(BusinessIdService.class).getNewId();

                    sourceDocument.setPropertyValue(Constants.TK_ID_FIELD, newId);
                }
                catch (NuxeoException e) {
                    event.markBubbleException();
                    throw e;

                }


            }
        }

    }


}
