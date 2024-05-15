package io.roqu.workspaces.paths;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.pathsegment.PathSegmentService;

/**
 * @author Loïc Billon
 */
public class WorkspacesPathSegmentServiceImpl implements PathSegmentService {

	protected final int maxSize = 64;
	
	/**
	 * Generates path segment without special chars and truncated
	 */
	@Override
	public String generatePathSegment(DocumentModel doc){
		String title = doc.getTitle();
		if (title == null) {
			title = StringUtils.EMPTY;
		}
		
		return IdUtils.generateId(title, "-", true, maxSize);		
	}
	
	/**
	 * Generates path segment:
	 * - value is lowercased
	 * - length is reduce
	 * - empty spaces are replaced by '-'.
	 */
	@Override
	public String generatePathSegment(String value)  {
		return IdUtils.generateId(value, "-", true, maxSize);
	}
	
	/**
	 * {@inheritDoc}
	 */
    @Override
    public int getMaxSize() {
        return maxSize;
    }

}
