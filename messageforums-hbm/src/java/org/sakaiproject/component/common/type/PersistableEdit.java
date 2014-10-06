package org.sakaiproject.component.common.type;

import java.util.Date;

/**
 * Allows us to get rid of reflection.
 *
 * @author Matthew Buckett
 */
public interface PersistableEdit {

	void setLastModifiedBy(String lastModifiedBy);

	void setLastModifiedDate(Date lastModifiedDate);

	void setCreatedBy(String createdBy);

	void setCreatedDate(Date createdDate);
}
