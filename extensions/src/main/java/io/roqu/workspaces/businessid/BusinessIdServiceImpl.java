package io.roqu.workspaces.businessid;

import io.roqu.workspaces.WorkspacesConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.directory.sql.SQLDirectory;
import org.nuxeo.ecm.directory.sql.SQLSession;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Loïc Billon
 */
public class BusinessIdServiceImpl extends DefaultComponent implements BusinessIdService {

    public final static int BUSINESS_ID_LENGTH = 8;
    
    private SQLSession session;
    
    @Override
    public String getNewId() throws NuxeoException {

        int totalTries = 0;
        do {
            try {
                String random = RandomStringUtils.random(BUSINESS_ID_LENGTH, true, true);
                Map<String, Object> newId = new HashMap<>();
                newId.put("id", random);
                getSession().createEntry(newId);
                return random;
            }
            catch (DirectoryException e) {
                totalTries++;
            }

        } while(totalTries <= 10);

        throw new NuxeoException("Unable to create a random business id.");

    }

    
    protected SQLSession getSession() {

        if(session == null || !session.isLive()) {
            DirectoryService directoryService = Framework.getService(DirectoryService.class);
            SQLDirectory sqlDir = (SQLDirectory) directoryService.getDirectory(WorkspacesConstants.BUSINESS_DIR);

            session = sqlDir.getSession();
        }
        return session;
    }

}
