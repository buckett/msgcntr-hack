/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/common/trunk/common-composite-component/src/java/org/sakaiproject/component/common/manager/PersistableHelper.java $
 * $Id: PersistableHelper.java 105077 2012-02-24 22:54:29Z ottenhoff@longsight.com $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.common.manager;

import org.sakaiproject.component.common.type.PersistableEdit;
import org.sakaiproject.tool.api.SessionManager;

import java.util.Date;

/**
 * Refactored this to get rid of the reflection.
 */
public class PersistableHelper
{
	private static final String SYSTEM = "SYSTEM";

	private SessionManager sessionManager; // dep inj

	public void modifyPersistableFields(PersistableEdit persistable)
	{
		if (persistable == null) throw new IllegalArgumentException("Illegal persistable argument passed!");

		String actor = getActor();
		Date now = new Date(); // time sensitive


		persistable.setLastModifiedBy(actor);
		persistable.setLastModifiedDate(now);

	}

	public void createPersistableFields(PersistableEdit persistable)
	{
		if (persistable == null) throw new IllegalArgumentException("Illegal persistable argument passed!");

		String actor = getActor();
		Date now = new Date(); // time sensitive

		persistable.setLastModifiedBy(actor);
		persistable.setLastModifiedDate(now);
		persistable.setCreatedBy(actor);
		persistable.setCreatedDate(now);
	}

	private String getActor()
	{
		String actor = sessionManager.getCurrentSessionUserId();
		if (actor == null || actor.length() < 1)
		{
			actor = SYSTEM;
		}
		return actor;
	}

	/**
	 * Dependency injection.
	 *
	 * @param sessionManager
	 *        The sessionManager to set.
	 */
	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}
}
