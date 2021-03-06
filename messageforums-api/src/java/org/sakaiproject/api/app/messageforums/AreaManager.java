/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/AreaManager.java $
 * $Id: AreaManager.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
 * The Area id the high level object of the object model. Typically a site
 * can contain up to 2 Areas - a Discussion Area and a Private Message Area
 * 
 * @author rshastri
 *
 */
public interface AreaManager
{
	/**
	 * Save an area. This is used to update settings on an area.
	 * @param area The area to update.
	 */
	public void saveArea(Area area);
	
	/**
	 * Create an area of the given type in the given site
	 * @param typeId the type id (private or discussion)
	 * @param siteId the site
	 * @return the created Area object
	 * @deprecated The {@link #getAreaByContextIdAndTypeId(String, String)} creates an area so this isn't needed.
	 */
	public Area createArea(String typeId, String siteId);
	
	/**
	 * Removes an area and everything within it.
	 * @param area The area to remove.
	 */
	public void deleteArea(Area area);
	
	/**
	 * Get an area of the given type
	 * @param typeId The type for an area.
	 * @return The area.
	 * @deprecated This gets the currrent site ID from the thread.
	 */
	public Area getAreaByContextIdAndTypeId(String typeId);
    
	
	/** Get an Area by site and type
	 * @param siteId The site ID.
	 * @param typeId The type for an area.
	 * @return The area
	 */
	public Area getAreaByContextIdAndTypeId(String siteId, String typeId);

	/**
	 * Get the private area for this site
	 * @deprecated No explicit site passed in.
	 * @see #getPrivateArea(String)
	 * @return The private area for this site, if one doesn't exist it will be created.
	 */
	public Area getPrivateArea();

	/**
	* Get the private area for this site
	 * @param siteId The site to get the private area for.
	* @return The private area for this site, if one doesn't exist it will be created.
		*/
	public Area getPrivateArea(String siteId);

	/**
	 * Get the discussion are for this site.
	 * @param siteId The site ID.
	 * @return The discussion area for the site.
	 */
	public Area getDiscussionArea(String siteId);
}
