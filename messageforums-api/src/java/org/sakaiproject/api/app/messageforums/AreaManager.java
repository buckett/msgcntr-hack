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
 * The Area id the high level object of the object model. Typicaly a site
 * can contain up to 2 Areas - a Discussion Area and a Private Message Area
 * 
 * @author rshastri
 *
 */


public interface AreaManager
{
	/**
	 * Is the private area enabled in this site?
	 * @return <code>true</code> is this area has a private messages area.
	 */
	public boolean isPrivateAreaEnabled();
	
	/**
	 * Save an area. This is used to update settings on an area.
	 * @param area The area to update.
	 */
	public void saveArea(Area area);

	/**
	 *
	 * @param area
	 * @param currentUser
	 * @deprecated Should be passing in the current user as it should be set on the thread.
	 */
	public void saveArea(Area area, String currentUser);
	
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
	 * Get all Areas of the given type
	 * @param typeId
	 * @return
	 * 
	 * @deprecated since Jan 2008, seems never to have been used and doesn't look to work as there will be lots of
	 * areas found.
	 */
	public Area getAreaByType(String typeId);
	
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
	 * @return The discussion area for the site.
	 * @see #getDiscussionArea(String)
	 * @deprecated No explicit site is passed in.
	 */
	public Area getDiscusionArea();

	/**
	 * Get the discussion are for this site.
	 * @param siteId The site ID.
	 * @return The discussion area for the site.
	 */
	public Area getDiscussionArea(String siteId);
	
	/**
	 * Get the discussion are for the given site
	 * @param siteId The site ID.
	 * @param createDefaultForum Should the default forum be created.
	 * @return The discussion area for the site.
	 * @deprecated This should need to be part of the public API as the reason for not creating the default
	 * forum is that you might be restoring an archive, but that should be done inside the service.
	 */
	public Area getDiscussionArea(final String siteId, boolean createDefaultForum);

	/**
	 * Get a resource bundle value.
	 * @param key The string to lookup.
	 * @return The translated string.
	 * @deprecated Doing resource bundle calls through the API isn't good as it creates a bigger API when we should
	 * have the bundle only really used in the tool.
	 */
	public String getResourceBundleString(String key);
}
