/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/AreaManagerImpl.java $
 * $Id: AreaManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.hibernate.collection.PersistentSet;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.ForumScheduleNotification;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.cover.ForumScheduleNotificationCover;
import org.sakaiproject.api.app.messageforums.cover.SynopticMsgcntrManagerCover;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * This is a DAO for Area.
 * Converted from HibernateTemplate to SessionFactory.
 */
public class AreaManagerImpl implements AreaManager {

    private static final Log LOG = LogFactory.getLog(AreaManagerImpl.class);
	private static final ResourceLoader rb = new ResourceLoader("org.sakaiproject.api.app.messagecenter.bundle.Messages");

    private static final String QUERY_AREA_BY_CONTEXT_AND_TYPE_ID = "findAreaByContextIdAndTypeId";
    private static final String QUERY_AREA_BY_TYPE = "findAreaByType";

    // TODO: pull titles from bundle
    private static final String MESSAGES_TITLE = "cdfm_message_pvtarea";
    private static final String FORUMS_TITLE = "cdfm_discussion_forums";

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private IdManager idManager;

    private SessionManager sessionManager;

    private MessageForumsTypeManager typeManager;

    private ServerConfigurationService serverConfigurationService;
    
    /**
     * sakai.property for setting the default Messages tool option for sending a copy of a message
     * to recipient email addresses. Options are {@link Area#EMAIL_COPY_NEVER}, {@link Area#EMAIL_COPY_OPTIONAL},
     * and {@link Area#EMAIL_COPY_ALWAYS}
     */
    private static final String DEFAULT_SEND_TO_EMAIL_PROP = "msgcntr.defaultSendToEmailSetting";

	public void init() {
		LOG.info("init()");

	}

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}


    public void setTypeManager(MessageForumsTypeManager typeManager) {
        this.typeManager = typeManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    public Area getPrivateArea() {
    	return getPrivateArea(getContextId());
    }
    
    public Area getPrivateArea(String siteId){
        Area area = getAreaByContextIdAndTypeId(siteId, typeManager.getPrivateMessageAreaType());
        if (area == null) {
            area = createArea(typeManager.getPrivateMessageAreaType(), siteId);
            area.setContextId(siteId);
            area.setName(rb.getString(MESSAGES_TITLE));
            area.setEnabled(Boolean.FALSE);
            area.setHidden(Boolean.TRUE);
            area.setLocked(Boolean.FALSE);
            area.setModerated(Boolean.FALSE);
            area.setPostFirst(Boolean.FALSE);
	    area.setAutoMarkThreadsRead(serverConfigurationService.getBoolean("msgcntr.forums.default.auto.mark.threads.read", false));
            area.setSendToEmail(serverConfigurationService.getInt(DEFAULT_SEND_TO_EMAIL_PROP, Area.EMAIL_COPY_OPTIONAL));
            saveArea(area);
        }

        return area;
    }
    
    public Area getDiscussionArea(String contextId) {

    	LOG.debug("getDiscussionArea(" + contextId +")");
    	if (contextId == null) {
    		throw new IllegalArgumentException("contextId can't be null");
    	}
    	Area area = this.getAreaByContextIdAndTypeId(contextId, typeManager.getDiscussionForumType());
    	
    	if (area == null) {
    		LOG.info("setting up a new Discussion Area for " + contextId);
    		area = createArea(typeManager.getDiscussionForumType(), contextId);
    		area.setName(rb.getString(FORUMS_TITLE));
            area.setEnabled(Boolean.TRUE);
            area.setHidden(Boolean.TRUE);
            area.setLocked(Boolean.FALSE);
            area.setModerated(Boolean.FALSE);
            area.setPostFirst(Boolean.FALSE);
            area.setAutoMarkThreadsRead(serverConfigurationService.getBoolean("msgcntr.forums.default.auto.mark.threads.read", false));
            // this is a Messages tool option
	    area.setSendToEmail(serverConfigurationService.getInt(DEFAULT_SEND_TO_EMAIL_PROP, Area.EMAIL_COPY_OPTIONAL));
	    area.setAvailabilityRestricted(Boolean.FALSE);
            saveArea(area);

            
    	}
    	
    	return area;
	}

    public Area createArea(String typeId, String contextParam) {
    	
    	  if (LOG.isDebugEnabled())
        {
          LOG.debug("createArea(" + typeId + "," + contextParam + ")");
        }
    	      	      	  
        Area area = new AreaImpl();
        area.setUuid(getNextUuid());
        area.setTypeUuid(typeId);
        area.setCreated(new Date());
        area.setCreatedBy(getCurrentUser());
        
        /** compatibility with web services*/
        if (contextParam == null){
        	String contextId = getContextId();
        	if (contextId == null){
        		throw new IllegalStateException("Cannot retrive current context");
        	}        	
        	area.setContextId(contextId);
        }
        else{
        	area.setContextId(contextParam);
        }                      
                                                                         
        LOG.debug("createArea executed with areaId: " + area.getUuid());
        return area;
    }

    /**
     * This method sets the modified user and date.  It then checks all the open forums for a 
     * sort index of 0.  (if a sort index on a forum is 0 then it is new). If there is a 
     * zero sort index then it increments all the sort indices by one so the new sort index
     * becomes the first without having to rely on the creation date for the sorting.
     * 
     * @param area Area to save
     */
    public void saveArea(Area area) {
    	String currentUser = getCurrentUser();
		
        boolean isNew = area.getId() == null;

        area.setModified(new Date());
        area.setModifiedBy(currentUser);
        
        boolean someForumHasZeroSortIndex = false;

        // If the open forums were not loaded then there is no need to redo the sort index
        //     thus if it's a hibernate persistentset and initialized
        if( area.getOpenForumsSet() != null &&
              ((area.getOpenForumsSet() instanceof PersistentSet && 
              ((PersistentSet)area.getOpenForumsSet()).wasInitialized()) || !(area.getOpenForumsSet() instanceof PersistentSet) )) {
           for(Iterator i = area.getOpenForums().iterator(); i.hasNext(); ) {
              BaseForum forum = (BaseForum)i.next();
              if(forum.getSortIndex().intValue() == 0) {
                 someForumHasZeroSortIndex = true;
                 break;
              }
           }
           if(someForumHasZeroSortIndex) {
              for(Iterator i = area.getOpenForums().iterator(); i.hasNext(); ) {
                 BaseForum forum = (BaseForum)i.next();
                 forum.setSortIndex(Integer.valueOf(forum.getSortIndex().intValue() + 1));
              }
           }
        }

        // until we have a settings screen to allow the user to hide all forums,
        // the area will always be available. 
        area.setAvailability(true); 
        
        sessionFactory.getCurrentSession().saveOrUpdate(area);

        LOG.debug("saveArea executed with areaId: " + area.getId());
    }

    public void deleteArea(Area area) {
		sessionFactory.getCurrentSession().delete(area);
        LOG.debug("deleteArea executed with areaId: " + area.getId());
    }

    /**
     * ContextId is present site id for now.
     */
    private String getContextId() {
        if (TestUtil.isRunningTests()) {
            return "test-context";
        }
        Placement placement = ToolManager.getCurrentPlacement();
        String presentSiteId = placement.getContext();
        return presentSiteId;
    }

    public Area getAreaByContextIdAndTypeId(final String typeId) {
        LOG.debug("getAreaByContextIdAndTypeId executing for current user: " + getCurrentUser());
        return this.getAreaByContextIdAndTypeId(getContextId(), typeId);
    }

	public Area getAreaByContextIdAndTypeId(final String contextId, final String typeId) {
		LOG.debug("getAreaByContextIdAndTypeId executing for current user: " + getCurrentUser());
		Session session = sessionFactory.getCurrentSession();
		Query q = session.getNamedQuery(QUERY_AREA_BY_CONTEXT_AND_TYPE_ID);
		q.setParameter("contextId", contextId, StandardBasicTypes.STRING);
		q.setParameter("typeId", typeId, StandardBasicTypes.STRING);
		return (Area) q.uniqueResult();

	}
    
    

    public Area getAreaByType(final String typeId) {
		final String currentUser = getCurrentUser();
		LOG.debug("getAreaByType executing for current user: " + currentUser);
		Session session = sessionFactory.getCurrentSession();
		Query q = session.getNamedQuery(QUERY_AREA_BY_TYPE);
		q.setParameter("typeId", typeId, StandardBasicTypes.STRING);
		return (Area) q.uniqueResult();
	}
       
    // helpers

    private String getNextUuid() {
        return idManager.createUuid();
    }

    private String getCurrentUser() {
    	String user = sessionManager.getCurrentSessionUserId();
  		return (user == null) ? "test-user" : user;
    }
}
