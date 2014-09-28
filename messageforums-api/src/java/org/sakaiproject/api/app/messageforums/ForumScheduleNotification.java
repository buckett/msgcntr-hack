package org.sakaiproject.api.app.messageforums;

import java.util.Date;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

/**
 * This probably doesn't need to be part of the API
 */
public interface ForumScheduleNotification {

	public void scheduleAvailability(Area area);
	
	public void scheduleAvailability(DiscussionForum forum);
	
	public void scheduleAvailability(DiscussionTopic topic);
	
	public boolean makeAvailableHelper(boolean availabilityRestricted, Date openDate, Date closeDate);
	
}
