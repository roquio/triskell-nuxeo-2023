package io.roqu.workspaces.businessid;

import io.roqu.workspaces.WorkspacesConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.directory.sql.SQLDirectory;
import org.nuxeo.ecm.directory.sql.SQLSession;
import org.nuxeo.runtime.api.Framework;

import java.util.HashMap;
import java.util.Map;

/**
 * This runner provide a generator of short business Ids.
 * The given IDs are stored in an SQL repository table with PK which garantee the unicity
 *
 * @author Loïc Billon
 */
public class BusinessIdRunner extends UnrestrictedSessionRunner {


    private String randomId;

    public final static int BUSINESS_ID_LENGTH = 8;

    protected BusinessIdRunner(CoreSession session) {
        super(session);
    }

    @Override
    public void run() {

        int totalTries = 0;

        DirectoryService directoryService = Framework.getService(DirectoryService.class);
        SQLDirectory sqlDir = (SQLDirectory) directoryService.getDirectory(WorkspacesConstants.BUSINESS_DIR);

        SQLSession sqlDirSession = sqlDir.getSession();

        do {
            try {

                String random = RandomStringUtils.random(BUSINESS_ID_LENGTH, true, true);
                Map<String, Object> newId = new HashMap<>();
                newId.put("id", random);

                sqlDirSession.createEntry(newId);
                randomId = random;

            }
            catch (DirectoryException e) {
                totalTries++;
            }

        } while(totalTries <= 10 && randomId == null);

        sqlDirSession.close();

        if(randomId == null) {
            throw new NuxeoException("Unable to create a random business id.");
        }

    }

    public String getRandomId() {
        return randomId;
    }
}
