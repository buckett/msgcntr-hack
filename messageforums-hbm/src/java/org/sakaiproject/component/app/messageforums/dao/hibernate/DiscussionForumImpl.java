/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/DiscussionForumImpl.java $
 * $Id: DiscussionForumImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.DateRestrictions;
import org.sakaiproject.api.app.messageforums.DiscussionForum;

public class DiscussionForumImpl extends OpenForumImpl implements DiscussionForum {

    private DateRestrictions dateRestrictions;
    private ActorPermissions actorPermissions;
    private Boolean autoMarkThreadsRead;

    public ActorPermissions getActorPermissions() {
        return actorPermissions;
    }

    public void setActorPermissions(ActorPermissions actorPermissions) {
        this.actorPermissions = actorPermissions;
    }

    public DateRestrictions getDateRestrictions() {
        return dateRestrictions;
    }

    public void setDateRestrictions(DateRestrictions dateRestrictions) {
        this.dateRestrictions = dateRestrictions;
    }

	public Boolean getAutoMarkThreadsRead() {
		return autoMarkThreadsRead;
	}

	public void setAutoMarkThreadsRead(Boolean autoMarkThreadsRead) {
		this.autoMarkThreadsRead = autoMarkThreadsRead;
	}

}
