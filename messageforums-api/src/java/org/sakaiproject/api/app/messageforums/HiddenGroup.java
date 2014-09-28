package org.sakaiproject.api.app.messageforums;

/**
 * This is a group that should not be shown in the messages interface.
 * @see Area#getHiddenGroups()
 */
public interface HiddenGroup {
	public Long getId();
	public void setId(Long id);
	public Integer getVersion();
	public void setVersion(Integer version);
	public Area getArea();
	public void setArea(Area area);
	
	public String getGroupId();
	public void setGroupId(String groupId);
}
