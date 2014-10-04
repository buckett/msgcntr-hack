/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/Attachment.java $
 * $Id: Attachment.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.messageforums;

/**
 * An attachment to a message/topic/forum.
 * There isn't any attachment manager at the moment, they are linked to other objects that do have services.
 * This might change in the future as we try to keep these detached from the other objects.
 *
 * All properties are copies from the underlying files in content hosting. Originally you could have a different
 * name to the display name of the file in content hosting but this was never used and so has been removed.
 *
 * @see org.sakaiproject.api.app.messageforums.Message
 * @see org.sakaiproject.api.app.messageforums.Topic
 * @see org.sakaiproject.api.app.messageforums.BaseForum - Link at the API level
 * @see org.sakaiproject.api.app.messageforums.OpenForum - Link at the DB level
 * @see org.sakaiproject.api.app.messageforums.PrivateForum
 */
public interface Attachment extends MutableEntity {

	/**
	 * This is the ID into content hosting.
	 * @return
	 */
    public String getAttachmentId();

    public void setAttachmentId(String attachmentId);

    public String getAttachmentName();

    public void setAttachmentName(String attachmentName);

    public String getAttachmentSize();

    public void setAttachmentSize(String attachmentSize);

    public String getAttachmentType();

    public void setAttachmentType(String attachmentType);
}