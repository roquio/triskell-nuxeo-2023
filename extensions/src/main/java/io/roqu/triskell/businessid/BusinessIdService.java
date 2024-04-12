package io.roqu.triskell.businessid;

import org.nuxeo.ecm.core.api.NuxeoException;

/**
 * This component provide a generator of short business Ids.
 * The given IDs are stored in a repository which garantee the unicity
 *
 * @author Loïc Billon
 */
public interface BusinessIdService {

    String getNewId() throws NuxeoException;

}
