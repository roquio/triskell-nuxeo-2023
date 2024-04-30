package io.roqu.triskell.paths;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.api.AbstractSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.pathsegment.PathSegmentService;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;


/**
 * Listener called in creation to set a clean path
 *
 * @author Loïc Billon
 */
public class PathsListener implements EventListener {

    @Override
    public void handleEvent(Event event) {

        if(event.getContext() instanceof DocumentEventContext docCtx) {
            DocumentModel sourceDocument = docCtx.getSourceDocument();

            if(StringUtils.isNotBlank(sourceDocument.getName())) {

                // Clean the segment path
                PathSegmentService pss = Framework.getService(PathSegmentService.class);
                String cleanSegmentName = pss.generatePathSegment(sourceDocument.getName());

                AbstractSession session = (AbstractSession) sourceDocument.getCoreSession();

                // Check the parent's children
                Serializable destinationPath = docCtx.getProperties().get(CoreEventConstants.DESTINATION_PATH);
                Document parent = session.getSession().resolvePath(destinationPath.toString());

                // Set options to force the new path
                docCtx.getProperties().put(CoreEventConstants.DESTINATION_EXISTS, parent.hasChild(cleanSegmentName));
                docCtx.getProperties().put(CoreEventConstants.DESTINATION_NAME, cleanSegmentName);

            }
        }

    }


}
