package org.sakaiproject.component.app.messageforums;

/**
 * This class should hold all outside of MessageCenter project references.
 * Having this class makes it much easier to mock up services for testing.
 *
 * @author Matthew Buckett
 */
interface SakaiProxy {

	String getCurrentUserId();

	String getUuid();

	String getCurrentSiteId();

	/**
	 * This gets the title of a tool in a site.
	 *
	 * @param siteId The site ID to find the tool in.
	 * @param toolId The registration ID of the tool (eg: sakai.forums).
	 */
	String getToolTitle(String siteId, String toolId);

}
