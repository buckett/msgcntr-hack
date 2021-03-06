package org.sakaiproject.api.app.messageforums.cover;

import java.util.Date;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.ForumScheduleNotification;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * @deprecated Shouldn't need covers.
 */
public class ForumScheduleNotificationCover {

	private static ForumScheduleNotification m_instance = null;
	
	public static ForumScheduleNotification getInstance(){
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (ForumScheduleNotification) ComponentManager
				.get(ForumScheduleNotification.class);
			return m_instance;
		}
		else
		{
			return (ForumScheduleNotification) ComponentManager
			.get(ForumScheduleNotification.class);
		}
	}
	
	public static void scheduleAvailability(Area area){
		ForumScheduleNotification service = getInstance();
		if(service != null){
			service.scheduleAvailability(area);
		}
	}
	
	public static void scheduleAvailability(DiscussionForum forum){
		ForumScheduleNotification service = getInstance();
		if(service != null){
			service.scheduleAvailability(forum);
		}
	}
	
	public static void scheduleAvailability(DiscussionTopic topic){
		ForumScheduleNotification service = getInstance();
		if(service != null){
			service.scheduleAvailability(topic);
		}
	}
	
	public static boolean makeAvailableHelper(boolean availabilityRestricted, Date openDate, Date closeDate){
		ForumScheduleNotification service = getInstance();
		if(service != null){
			return service.makeAvailableHelper(availabilityRestricted, openDate, closeDate);
		}
		//when it doubt return true
		return true;
	}
}
