package io.roqu.workspaces.paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private final static String SYSTEM_USER = "system";

    private final static Log log = LogFactory.getLog(PathsListener.class);

    @Override
    public void handleEvent(Event event) {

        try {

            if (event.getContext() instanceof DocumentEventContext docCtx) {

                // Skip events fired by the system user
                // If path is modified during import, it may be fail (ex : import of document-route-model).
                // TODO check the silent modifications
                if (!event.getContext().getPrincipal().getName().equals(SYSTEM_USER)) {

                    if(log.isDebugEnabled()) {
                        log.debug(docCtx.getSourceDocument());
                    }

                    DocumentModel sourceDocument = docCtx.getSourceDocument();

                    if (StringUtils.isNotBlank(sourceDocument.getName())) {

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

                        if(log.isDebugEnabled()) {
                            log.debug(sourceDocument.getName() + "=>" + cleanSegmentName);
                        }

                    }

                } else {
                    if(log.isDebugEnabled()) {
                        log.debug("skip " + docCtx.getSourceDocument());
                    }

                }
            }

        } catch (Exception e) {
            log.error(e);
        }

    }


}
