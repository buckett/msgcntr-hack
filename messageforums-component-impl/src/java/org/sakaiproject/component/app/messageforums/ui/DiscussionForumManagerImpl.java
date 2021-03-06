/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/ui/DiscussionForumManagerImpl.java $
 * $Id: DiscussionForumManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.component.app.messageforums.ui;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.DummyDataHelperApi;
import org.sakaiproject.api.app.messageforums.MembershipManager;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.component.app.messageforums.AttachmentService;
import org.sakaiproject.component.app.messageforums.dao.hibernate.ActorPermissionsImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DBMembershipItemImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageForumsUserImpl;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;


/**
 * This is designed to be the higher level service for the discussion forum.
 *
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public class DiscussionForumManagerImpl implements DiscussionForumManager {
  private static final String MC_DEFAULT = "mc.default.";
  private static final Log LOG = LogFactory
      .getLog(DiscussionForumManagerImpl.class);
  private AreaManager areaManager;
  private MessageForumsForumManager forumManager;
  private MessageForumsMessageManager messageManager;
  private DummyDataHelperApi helper;
  private MessageForumsTypeManager typeManager;
  private SiteService siteService;
  private UserDirectoryService userDirectoryService;
  private MembershipManager membershipManager;
  private SecurityService securityService;
  private SessionManager sessionManager;
  private PermissionLevelManager permissionLevelManager;
  private Map courseMemberMap = null;
  private boolean usingHelper = false; // just a flag until moved to database from helper
  private AttachmentService attachmentService;
  private MemoryService memoryService;
  private Cache allowedFunctionsCache;
  
  public static final int MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST = 1000;

  public void init()
  {
     LOG.info("init()");
	  // So crazy that the authz service is re-implemented.
     allowedFunctionsCache = memoryService.newCache("org.sakaiproject.component.app.messageforums.ui.DiscussionForumManagerImpl.allowedFunctionsCache");
  }

	public void setAttachmentService(AttachmentService attachmentService) {
		this.attachmentService = attachmentService;
	}

	public List searchTopicMessages(Long topicId, String searchText)
  {
    return forumManager.searchTopicMessages(topicId, searchText);
  }

  public Topic getTopicByIdWithAttachments(Long topicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicByIdWithAttachments(Long " + topicId + ")");
    }
    return forumManager.getTopicByIdWithAttachments(topicId);
  }

  public List getTopicsByIdWithMessages(final Long forumId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicsByIdWithMessages(final Long" + forumId + ")");
    }
    return forumManager.getTopicsByIdWithMessages(forumId);
  }

  public List getTopicsByIdWithMessagesAndAttachments(final Long forumId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicsByIdWithMessagesAndAttachments(final Long" + forumId
          + ")");
    }
    return forumManager.getTopicsByIdWithMessagesAndAttachments(forumId);
  }
  
  public List getTopicsByIdWithMessagesMembershipAndAttachments(final Long forumId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicsByIdWithMessagesMembershipAndAttachments(final Long" + forumId
          + ")");
    }
    return forumManager.getTopicsByIdWithMessagesMembershipAndAttachments(forumId);
  }

  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getForumsForMainPage()
   */
  public List<DiscussionForum> getForumsForMainPage() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("getForumsForMainPage()");
    }
    return forumManager.getForumsForMainPage();
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getMessageCountsForMainPage(java.util.List)
   */
  public List<Object[]> getMessageCountsForMainPage(Collection<Long> topicIds) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("getMessageCountsForMainPage(" + topicIds + ")");
    }
    return messageManager.findMessageCountsForMainPage(topicIds);
  }

  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getMessageCountsForMainPage(java.util.Collection)
   */
  public List<Object[]> getReadMessageCountsForMainPage(Collection<Long> topicIds) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("getReadMessageCountsForMainPage(" + topicIds + ")");
    }
    return messageManager.findReadMessageCountsForMainPage(topicIds);
  }
  
  public Topic getTopicByIdWithMessages(final Long topicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicByIdWithMessages(final Long" + topicId + ")");
    }
    return forumManager.getTopicByIdWithMessages(topicId);
  }
  
  public Topic getTopicWithAttachmentsById(final Long topicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicWithAttachmentsById(final Long" + topicId + ")");
    }
    return forumManager.getTopicWithAttachmentsById(topicId);
  }

  public Topic getTopicByIdWithMessagesAndAttachments(final Long topicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicByIdWithMessagesAndAttachments(final Long" + topicId
          + ")");
    }
    return forumManager.getTopicByIdWithMessagesAndAttachments(topicId);
  }
  
  public List getModeratedTopicsInSite()
  {
	  if (LOG.isDebugEnabled())
	  {
		  LOG.debug("getModeratedTopicsInSite()");
	  }
	  return forumManager.getModeratedTopicsInSite(ToolManager.getCurrentPlacement().getContext());
  }

  // start injection
  /**
   * @param helper
   */
  public void setHelper(DummyDataHelperApi helper)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setHelper(DummyDataHelperApi " + helper + ")");
    }
    this.helper = helper;
  }

  /**
   * @param areaManager
   */
  public void setAreaManager(AreaManager areaManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setAreaManager(AreaManager" + areaManager + ")");
    }
    this.areaManager = areaManager;
  }

  /**
   * @param permissionLevelManager
   *          The permissionLevelManager to set.
   */
  public void setPermissionLevelManager(
      PermissionLevelManager permissionLevelManager)
  {
    this.permissionLevelManager = permissionLevelManager;
  }

  /**
   * @param typeManager
   *          The typeManager to set.
   */
  public void setTypeManager(MessageForumsTypeManager typeManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setTypeManager(MessageForumsTypeManager" + typeManager + ")");
    }
    this.typeManager = typeManager;
  }

  /**
   * @param siteService
   *          The siteService to set.
   */
  public void setSiteService(SiteService siteService)
  {
    this.siteService = siteService;
  }

  /**
   * @param sessionManager
   *          The sessionManager to set.
   */
  public void setSessionManager(SessionManager sessionManager)
  {
    this.sessionManager = sessionManager;
  }

  /**
   * @param securityService
   *          The securityService to set.
   */
  public void setSecurityService(SecurityService securityService)
  {
    this.securityService = securityService;
  }

  /**
   * @param userDirectoryService
   *          The userDirectoryService to set.
   */
  public void setUserDirectoryService(UserDirectoryService userDirectoryService)
  {
    this.userDirectoryService = userDirectoryService;
  }

  /**
   * @param membershipManager
   *          The membershipManager to set.
   */
  public void setMembershipManager(MembershipManager membershipManager)
  {
    this.membershipManager = membershipManager;
  }

  /**
   * @return
   */
  public MessageForumsMessageManager getMessageManager()
  {

    LOG.debug("getMessageManager()");

    return messageManager;
  }

  /**
   * @param messageManager
   */
  public void setMessageManager(MessageForumsMessageManager messageManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setMessageManager(MessageForumsMessageManager"
          + messageManager + ")");
    }
    this.messageManager = messageManager;
  }

  // end injection

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDiscussionForumArea()
   */
  public Area getDiscussionForumArea()
  {
	return getDiscussionForumArea(ToolManager.getCurrentPlacement().getContext());  
  }
  
  public Area getDiscussionForumArea(String siteId)
  {
    LOG.debug("getDiscussionForumArea");

    if (usingHelper)
    {
      return helper.getDiscussionForumArea();
    }
    return areaManager.getDiscussionArea(siteId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getMessageById(java.lang.Long)
   */
  public Message getMessageById(Long id)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getMessageById( Long" + id + ")");
    }
    if (usingHelper)
    {
      return helper.getMessageById(id);
    }
    return messageManager.getMessageById(id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveMessage(org.sakaiproject.api.app.messageforums.Message)
   */
  public void saveMessage(Message message) {
	  saveMessage(message, true);
  }
  
  public void saveMessage(Message message, boolean logEvent) {
      saveMessage(message, logEvent, false);
  }
  
  public void saveMessage(Message message, boolean logEvent, boolean ignoreLockedTopicForum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveMessage(Message " + message + ")");
    }
    if (message.getTopic().getBaseForum() == null)
    {
      message.setTopic(getTopicById(message.getTopic().getId()));
    }
    if(this.getAnonRole()==true&&message.getCreatedBy()==null)
    {
    	message.setCreatedBy(".anon");
    }
    if(this.getAnonRole()==true&&message.getModifiedBy()==null)
    {
    	message.setModifiedBy(".anon");
    }
    
    messageManager.saveMessage(message, logEvent, ignoreLockedTopicForum);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#deleteMessage(org.sakaiproject.api.app.messageforums.Message)
   */
  public void deleteMessage(Message message)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("deleteMessage(Message" + message + ")");
    }
    messageManager.deleteMessage(message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getTotalNoMessages(org.sakaiproject.api.app.messageforums.Topic)
   */
  public int getTotalNoMessages(Topic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTotalNoMessages(Topic" + topic + ")");
    }
    if (usingHelper)
    {
      return 20;
    }
    return messageManager.findMessageCountByTopicId(topic.getId());
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getTotalViewableMessagesWhenMod(org.sakaiproject.api.app.messageforums.Topic)
   */
  public int getTotalViewableMessagesWhenMod(Topic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTotalViewableMessagesWhenMod(Topic" + topic + ")");
    }
    if (usingHelper)
    {
      return 20;
    }
    return messageManager.findViewableMessageCountByTopicId(topic.getId());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getUnreadNoMessages(org.sakaiproject.api.app.messageforums.Topic)
   */
  public int getUnreadNoMessages(Topic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getUnreadNoMessages(Topic" + topic + ")");
    }
    if (usingHelper)
    {
      return 10;
    }
    return messageManager.findUnreadMessageCountByTopicId(topic.getId());
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getUnreadApprovedNoMessages(org.sakaiproject.api.app.messageforums.Topic)
   */
  public int getNumUnreadViewableMessagesWhenMod(Topic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getNumUnreadViewableMessagesWhenMod(Topic" + topic + ")");
    }
    if (usingHelper)
    {
      return 10;
    }
    return messageManager.findUnreadViewableMessageCountByTopicId(topic.getId());
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#approveAllPendingMessages(java.lang.Long)
   */
  public void approveAllPendingMessages(Long topicId)
  {
	  if (topicId == null)
	  {
		  LOG.error("approveAllPendingMessages failed with topicId: Null" );
          throw new IllegalArgumentException("Null Argument");
	  }
	  List messages = this.getMessagesByTopicId(topicId);
	  if (messages != null && messages.size() > 0)
	  {
		  Iterator msgIter = messages.iterator();
		  while (msgIter.hasNext())
		  {
			  Message msg = (Message) msgIter.next();
			  if (msg.getApproved() == null)
			  {
				  msg.setApproved(Boolean.TRUE);
			  }
		  }
	  }
  }
  
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getTotalNoPendingMessages()
   */
  public List getPendingMsgsInSiteByMembership(List membershipList)
  {
	  return messageManager.getPendingMsgsInSiteByMembership(membershipList);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDiscussionForums()
   */
  public List getDiscussionForums()
  {
    LOG.debug("getDiscussionForums()");
    if (usingHelper)
    {
      return helper.getDiscussionForumArea().getDiscussionForums();
    }
    return forumManager.getForumByTypeAndContext(typeManager
        .getDiscussionForumType());
    // return getDiscussionForumArea().getDiscussionForums();
  }
  public List getDiscussionForums(String siteId)
  {
    LOG.debug("getDiscussionForums(siteId)");
    if (usingHelper)
    {
      return helper.getDiscussionForumArea().getDiscussionForums();
    }
    return forumManager.getForumByTypeAndContext(typeManager
        .getDiscussionForumType(), siteId);
    // return getDiscussionForumArea().getDiscussionForums();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDiscussionForumsByContextId()
   */
  public List getDiscussionForumsByContextId(String contextId)
  {
    LOG.debug("getDiscussionForumsByContextId(String contextId)");
    
    return forumManager.getForumByTypeAndContext(typeManager
        .getDiscussionForumType(), contextId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getForumById(java.lang.Long)
   */
  public DiscussionForum getForumById(Long forumId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getForumById(Long" + forumId + ")");
    }
    if (usingHelper)
    {
      return helper.getForumById(forumId);
    }
    return (DiscussionForum) forumManager.getForumById(true, forumId);
  }

  public DiscussionForum getForumByUuid(String forumId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getForumByUuid(String" + forumId + ")");
    }
    return (DiscussionForum) forumManager.getForumByUuid(forumId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getMessagesByTopicId(java.lang.Long)
   */
  public List getMessagesByTopicId(Long topicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getMessagesByTopicId(Long" + topicId + ")");
    }
    return messageManager.findMessagesByTopicId(topicId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getTopicById(java.lang.Long)
   */
  public DiscussionTopic getTopicById(Long topicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicById(Long" + topicId + ")");
    }

    return (DiscussionTopic) forumManager.getTopicById(true, topicId);
  }

  public DiscussionForum getForumByIdWithTopics(Long forumId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getForumByIdWithTopics(Long" + forumId + ")");
    }
    return (DiscussionForum) forumManager.getForumByIdWithTopics(forumId);
  }
  
  public DiscussionForum getForumByIdWithTopicsAttachmentsAndMessages(Long forumId) {
	  if (LOG.isDebugEnabled()) { LOG.debug("getForumByIdWithTopicsAttachmentsAndMessages(Long " + forumId + ")"); }
	  return (DiscussionForum) forumManager.getForumByIdWithTopicsAttachmentsAndMessages(forumId);
  }

  public DiscussionTopic getTopicByUuid(String topicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug(" getTopicByUuid(String" + topicId + ")");
    }
    return (DiscussionTopic) forumManager.getTopicByUuid(topicId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#hasNextTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean hasNextTopic(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("hasNextTopic(DiscussionTopic" + topic + ")");
    }
    if (usingHelper)
    {
      return helper.hasNextTopic(topic);
    }

    // TODO: Needs optimized
    boolean next = false;
    DiscussionForum forum = getForumById(topic.getBaseForum().getId());
    if (forum != null && forum.getTopics() != null)
    {
      for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();)
      {
    	  try{
        DiscussionTopic t = (DiscussionTopic) iter.next();
        if (next && getTopicAccess(t))
        {
          return true;
        }
        if (t != null && getTopicAccess(t))
        {
          if (t.getId().equals(topic.getId()))
          {
            next = true;
          }
        }
    	  }catch (Exception e) {
    		  LOG.error(e.getMessage());
		}
      }
    }

    // if we get here, there is no next topic
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#hasPreviousTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean hasPreviousTopic(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("hasPreviousTopic(DiscussionTopic" + topic + ")");
    }
    if (usingHelper)
    {
      return helper.hasPreviousTopic(topic);
    }

    // TODO: Needs optimized
    DiscussionTopic prev = null;
    DiscussionForum forum = getForumById(topic.getBaseForum().getId());
    if (forum != null && forum.getTopics() != null)
    {
      for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();)
      {
        DiscussionTopic t = (DiscussionTopic) iter.next();
        if (t != null && getTopicAccess(t))
        {
          if (t.getId().equals(topic.getId()))
          {
            // need to check null because we might be on the first topic
            // which means there is no previous one
            return prev != null;
          }
          prev = (DiscussionTopic) t;
        }
      }
    }

    // if we get here, there is no previous topic
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getNextTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public DiscussionTopic getNextTopic(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getNextTopic(DiscussionTopic" + topic + ")");
    }
    if (usingHelper)
    {
      if (hasNextTopic(topic))
      {
        return helper.getNextTopic(topic);
      }
      else
      {
        return null;
      }
    }

    // TODO: Needs optimized and re-written to take advantage of the db... this is really horrible.
    boolean next = false;
    DiscussionForum forum = getForumById(topic.getBaseForum().getId());
    if (forum != null && forum.getTopics() != null)
    {
      for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();)
      {
        DiscussionTopic t = (DiscussionTopic) iter.next();
        if (next && getTopicAccess(t))
        {
          if (t == null)
          {
            do
            {
              t = (DiscussionTopic) iter.next();
            } while (t == null);
          }
          return (DiscussionTopic) t;
        }
        if (t != null && getTopicAccess(t))
        {
          if (t.getId().equals(topic.getId()))
          {
            next = true;
          }
        }
      }
    }

    // if we get here, there is no next topic
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getPreviousTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public DiscussionTopic getPreviousTopic(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getPreviousTopic(DiscussionTopic" + topic + ")");
    }
    if (usingHelper)
    {
      if (hasPreviousTopic(topic))
      {
        return helper.getPreviousTopic(topic);
      }
      else
      {
        return null;
      }
    }
    // TODO: Needs optimized
    DiscussionTopic prev = null;
    DiscussionForum forum = getForumById(topic.getBaseForum().getId());
    if (forum != null && forum.getTopics() != null)
    {
      for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();)
      {
        DiscussionTopic t = (DiscussionTopic) iter.next();
        if (t != null && getTopicAccess(t))
        {
          if (t.getId().equals(topic.getId()))
          {
            return prev;
          }
          if (t != null && getTopicAccess(t))
          {
            prev = (DiscussionTopic) t;
          }
        }
      }
    }

    // if we get here, there is no previous topic
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#isInstructor()
   */
  public boolean isInstructor()
  {
    LOG.debug("isInstructor()");
    return isInstructor(userDirectoryService.getCurrentUser());
  }
  
  public boolean isInstructor(String userId)
  {
    LOG.debug("isInstructor()");
    try {
		return isInstructor(userDirectoryService.getUser(userId));
	} catch (UserNotDefinedException e) {
		LOG.error("DiscussionForumManagerImpl: isInstructor(String userId, String siteId): " + e.getMessage());
		return false;
	}
  }

  public boolean isInstructor(String userId, String siteId) {
    LOG.debug("isInstructor(String " + userId + ", " + siteId + ")");
    try {
		return isInstructor(userDirectoryService.getUser(userId), siteId);
	} catch (UserNotDefinedException e) {
		LOG.debug("DiscussionForumManagerImpl: isInstructor(String userId, String siteId): " + e.getMessage());
		return false;
	}
  }

  /**
   * Check if the given user has site.upd access
   * 
   * @param user
   * @return
   */
  public boolean isInstructor(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isInstructor(User " + user + ")");
    }
    if (user != null)
      return isInstructor(user, getContextSiteId());
    else
      return false;
  }
  
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#isInstructor()
   */
  public boolean isSectionTA()
  {
    LOG.debug("isSectionTA()");
    return isSectionTA(userDirectoryService.getCurrentUser());
  }

  
  /**
   * Check if the given user has site.upd access
   * 
   * @param user
   * @param siteId
   * @return
   */
  public boolean isInstructor(User user, String siteId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isInstructor(User " + user + ", " + siteId + ")");
    }
    if (user != null)
      return securityService.unlock(user, "site.upd", siteId);
    else
      return false;
  }

  /**
   * Check if the given user has section.role.ta access
   * 
   * @param user
   * @return
   */
  private boolean isSectionTA(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isSectionTA(User " + user + ")");
    }
    if (user != null)
      return securityService.unlock(user, "section.role.ta", getContextSiteId());
    else
      return false;
  }

  /**
   * @return siteId
   */
  private String getContextSiteId()
  {
    LOG.debug("getContextSiteId()");
    return "/site/" + getCurrentContext();
  }
  
  /**
   * 
   * @return the current context without the "/site/" prefix
   */
  private String getCurrentContext() {
      return ToolManager.getCurrentPlacement().getContext();
  }
  
  private String getCurrentUser() {
      return sessionManager.getCurrentSessionUserId();
  }

  /**
   * @param forumManager
   */
  public void setForumManager(MessageForumsForumManager forumManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setForumManager(MessageForumsForumManager" + forumManager
          + ")");
    }
    this.forumManager = forumManager;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#createForum()
   */
  public DiscussionForum createForum()
  {
    LOG.debug("createForum()");
    return forumManager.createDiscussionForum();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#deleteForum(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public void deleteForum(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setForumManager(DiscussionForum" + forum + ")");
    }
    forumManager.deleteDiscussionForum(forum);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#createTopic(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public DiscussionTopic createTopic(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("createTopic(DiscussionForum" + forum + ")");
    }
    if (forum == null)
    {
      LOG.debug("Attempt to create topic with out forum");
      return null;
    }
    return forumManager.createDiscussionForumTopic(forum);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveForum(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public void saveForum(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveForum(DiscussionForum" + forum + ")");
    }
    saveForum(forum, false, getCurrentContext(), true);
  }
  
  public void saveForum(String contextId, DiscussionForum forum) {
      if (LOG.isDebugEnabled()) LOG.debug("saveForum(String contextId, DiscussionForum forum)");
      
      if (contextId == null || forum == null) {
          throw new IllegalArgumentException("Null contextId or forum passed to saveForum. contextId:" + contextId);
      }
      
      saveForum(forum, forum.getDraft(), contextId, true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveForumAsDraft(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public void saveForumAsDraft(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveForumAsDraft(DiscussionForum" + forum + ")");
    }
    saveForum(forum, true, getCurrentContext(), true);
  }

	// The sole purpose of the the current user is so that the quartz job can be a different user
	// quartz jobs should just set the current user on the thread.
  public void saveForum(DiscussionForum forum, boolean draft, String contextId, boolean logEvent)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveForum(DiscussionForum" + forum + "boolean " + draft + ")");
    }

    boolean saveArea = forum.getId() == null;
    forum.setDraft(Boolean.valueOf(draft));
//    ActorPermissions originalForumActorPermissions = null;
//    if (saveArea)
//    {
//      originalForumActorPermissions = new ActorPermissionsImpl();
//    }
//    else
//    {
//      originalForumActorPermissions = forum.getActorPermissions();
//    }
//    // setcontributors
//    List holdContributors = new ArrayList();
//    holdContributors = Arrays.asList(forum.getActorPermissions()
//        .getContributors().toArray());
//    originalForumActorPermissions.setContributors(new UniqueArrayList());// clearing list at this
//    // point.
//    if (holdContributors != null && holdContributors.size() > 0)
//    {
//      Iterator iter = holdContributors.iterator();
//      while (iter.hasNext())
//      {
//        MessageForumsUser user = (MessageForumsUser) iter.next();
//        forum.getActorPermissions().addContributor(user);
//      }
//    }
//    // setAccessors
//    List holdAccessors = new ArrayList();
//    holdAccessors = Arrays.asList(forum.getActorPermissions().getAccessors()
//        .toArray());
//    originalForumActorPermissions.setAccessors(new UniqueArrayList());// clearing list at this point.
//    if (holdAccessors != null && holdAccessors.size() > 0)
//    {
//      Iterator iter = holdAccessors.iterator();
//      while (iter.hasNext())
//      {
//        MessageForumsUser user = (MessageForumsUser) iter.next();
//        forum.getActorPermissions().addAccesssor(user);
//      }
//    }
    
    forumManager.saveDiscussionForum(forum, draft, logEvent);
    //set flag to false since permissions could have changed.  This will force a clearing and resetting
    //of the permissions cache.
    ThreadLocalManager.set(UIPermissionsManager.MESSAGE_CENTER_PERMISSION_SET, Boolean.valueOf(false));
    if (saveArea)
    {
      //Area area = getDiscussionForumArea();
      String dfType = typeManager.getDiscussionForumType();
      Area area = areaManager.getAreaByContextIdAndTypeId(contextId, dfType);
      forum.setArea(area);
      forum.setSortIndex(Integer.valueOf(0));
      area.addDiscussionForum(forum);
      areaManager.saveArea(area);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public void saveTopic(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveTopic(DiscussionTopic" + topic + ")");
    }
    saveTopic(topic, false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveTopicAsDraft(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public void saveTopicAsDraft(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveTopicAsDraft(DiscussionTopic" + topic + ")");
    }
    saveTopic(topic, true);
  }

  private void saveTopic(DiscussionTopic topic, boolean draft)
  {
	  saveTopic(topic, draft, true);
  }
  
  public void saveTopic(DiscussionTopic topic, boolean draft, boolean logEvent)
  {
    LOG
        .debug("saveTopic(DiscussionTopic " + topic + ", boolean " + draft
            + ")");

    boolean saveForum = topic.getId() == null;
    
    topic.setDraft(Boolean.valueOf(draft));
    forumManager.saveDiscussionForumTopic(topic, false, logEvent);
    
    if (saveForum)
    {
      DiscussionForum forum = (DiscussionForum) topic.getBaseForum();
      forum.addTopic(topic);
      forumManager.saveDiscussionForum(forum, forum.getDraft().booleanValue(), logEvent);
      //sak-5146 forumManager.saveDiscussionForum(forum);
    }
    
    if(logEvent){
    	if (topic.getId() == null) {
    		EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_ADD, getEventMessage(topic), false));
    	} else {
    		EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_REVISE, getEventMessage(topic), false));
    	}
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#deleteTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public void deleteTopic(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("deleteTopic(DiscussionTopic " + topic + ")");
    }
    forumManager.deleteDiscussionForumTopic(topic);
  }

  /**
   * @return Roles for the current site
   */
  private Iterator getRoles()
  {
    LOG.trace("getRoles()");
    List roleList = new ArrayList();
    AuthzGroup realm = null;
    try
    {
      realm = AuthzGroupService.getAuthzGroup(getContextSiteId());
      Set roles = realm.getRoles();
      if (roles != null && roles.size() > 0)
      {
        Iterator roleIter = roles.iterator();
        while (roleIter.hasNext())
        {
          Role role = (Role) roleIter.next();
          if (role != null) roleList.add(role.getId());
        }
      }
    }
    catch (GroupNotDefinedException e) {
		LOG.warn("Failed to find realm for site: "+ getContextSiteId(), e);
	}
    Collections.sort(roleList);
    return roleList.iterator();
  }
  
  public boolean  getAnonRole()
  {
	  return getAnonRole(getContextSiteId());
  }
  
  public boolean  getAnonRole(String contextSiteId)
  {
   LOG.debug("getAnonRoles()");
   AuthzGroup realm = null;
   try
    {
      realm = AuthzGroupService.getAuthzGroup(contextSiteId);      
      Role anon = realm.getRole(".anon");
     if (sessionManager.getCurrentSessionUserId()==null && anon != null && anon.getAllowedFunctions().contains("site.visit"))
      {
			return true;
      }
    }       

    catch (GroupNotDefinedException e) {
		
		e.printStackTrace();
		return false;
	}      
    return false; 
  }

  public void markMessageAs(Message message, boolean readStatus)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("markMessageAsRead(Message" + message + ")");
    }
    try
    {
      messageManager.markMessageReadForUser(message.getTopic().getId(), message
          .getId(), readStatus);
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
    }

  }
  
  public void markMessageReadStatusForUser(Message message, boolean readStatus, String userId)
  {
	  if (LOG.isDebugEnabled())
	  {
		  LOG.debug("markMessageReadStatusForUser(Message" + message + " readStatus:" + readStatus + " userId: " + userId + ")");
	  }
	  try
	  {
		  messageManager.markMessageReadForUser(message.getTopic().getId(), message
				  .getId(), readStatus, userId);
	  }
	  catch (Exception e)
	  {
		  LOG.error(e.getMessage(), e);
	  }
  }
  
  /**
   * @param forum
   * @return
   */
  
  public boolean isForumOwner(DiscussionForum forum){
	  return isForumOwner(forum, userDirectoryService.getCurrentUser().getId());
  }
  
  public boolean isForumOwner(DiscussionForum forum, String userId)
  {
	return isForumOwner(forum, userId, getContextSiteId());
  }
  
  public boolean isForumOwner(DiscussionForum forum, String userId, String siteId)
  {
	  return isForumOwner(forum.getId(), forum.getCreatedBy(), userId, siteId);
  }
  
  public boolean isForumOwner(Long forumId, String forumCreatedBy, String userId, String siteId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isForumOwner(DiscussionForum " + forumId + ")");
    }
    if (forumCreatedBy.equals(userId) && !isRoleSwapView(siteId))
    {
      return true;
    }
    return false;
  }
  
  private boolean isRoleSwapView(String siteId)
  {
	return (securityService.getUserEffectiveRole(siteId) != null);
  }

  /**
   * @param topic
   * @return
   */
  
  public boolean isTopicOwner(DiscussionTopic topic){
	  return isTopicOwner(topic, userDirectoryService.getCurrentUser().getId());
  }
  
  public boolean isTopicOwner(DiscussionTopic topic, String userId)
  {
	  return isTopicOwner(topic, userId, getContextSiteId());
  }
  
  public boolean isTopicOwner(DiscussionTopic topic, String userId, String siteId)
  {
	  return isTopicOwner(topic.getId(), topic.getCreatedBy(), userId, siteId);
  }
  
  public boolean isTopicOwner(Long topicId, String topicCreatedBy, String userId, String siteId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isTopicOwner(DiscussionTopic " + topicId + ")");
    }
    if (topicCreatedBy.equals(userId) && !isRoleSwapView(siteId))
    {
      return true;
    }
    return false;
  }

  private boolean getTopicAccess(DiscussionTopic t)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicAccess(DiscussionTopic" + t + ")");
    }

    // SAK-27570: Return early instead of looping through lots of database records
    if (isInstructor() || securityService.isSuperUser() || isTopicOwner(t)) {
      return true;
    }
    else if (t.getDraft().equals(Boolean.TRUE) || t.getAvailability() == null || !t.getAvailability()) {
    	return false;
    }

    //SAK-12685 If topic's permission level name is "None", then can't access 
    User user=userDirectoryService.getCurrentUser();
    String role=AuthzGroupService.getUserRole(user.getId(), getContextSiteId());
    return !forumManager.doesRoleHavePermissionInTopic(t.getId(), role, PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getAllCourseMembers()
   */
  public Map getAllCourseMembers()
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getAllCourseMembers()");
    }
    if (courseMemberMap == null)
    {
      courseMemberMap = membershipManager.getAllCourseMembers(true, false, true, null);
    }
    return courseMemberMap;
  }

  /**
   *          The courseMemberMap to set.
   */
  public void setCourseMemberMapToNull()
  {
    this.courseMemberMap = null;
  }

  private ActorPermissions getDeepCopyOfParentActorPermissions(
      ActorPermissions actorPermissions)
  {
    ActorPermissions newAP = new ActorPermissionsImpl();
    Set parentAccessors = actorPermissions.getAccessors();
    Set parentContributors = actorPermissions.getContributors();
    Set newAccessors = new LinkedHashSet();
    Set newContributor = new LinkedHashSet();
    Iterator iter = parentAccessors.iterator();
    while (iter.hasNext())
    {
      MessageForumsUser accessParent = (MessageForumsUser) iter.next();
      MessageForumsUser newaccessor = new MessageForumsUserImpl();
      newaccessor.setTypeUuid(accessParent.getTypeUuid());
      newaccessor.setUserId(accessParent.getUserId());
      newaccessor.setUuid(accessParent.getUuid());
      newAccessors.add(newaccessor);
    }
    Iterator iter1 = parentContributors.iterator();
    while (iter1.hasNext())
    {
      MessageForumsUser contribParent = (MessageForumsUser) iter1.next();
      MessageForumsUser newcontributor = new MessageForumsUserImpl();
      newcontributor.setTypeUuid(contribParent.getTypeUuid());
      newcontributor.setUserId(contribParent.getUserId());
      newcontributor.setUuid(contribParent.getUuid());
      newContributor.add(newcontributor);
    }
    newAP.setAccessors(newAccessors);
    newAP.setContributors(newContributor);
    return newAP;
  }

  public DBMembershipItem getAreaDBMember(Set originalSet, String name,
      Integer type)
  {
    DBMembershipItem newItem = getDBMember(originalSet, name, type);
    return newItem;
  }
  
  public DBMembershipItem getDBMember(Set originalSet, String name,
			Integer type) {
	  return getDBMember(originalSet, name, type, getContextSiteId());
	}

  public DBMembershipItem getDBMember(Set originalSet, String name,
      Integer type, String contextSiteId)
  {
      	
    DBMembershipItem membershipItem = null;
    DBMembershipItem membershipItemIter;
    
    if (originalSet != null){
      Iterator iter = originalSet.iterator();
      while (iter.hasNext())
      {
      	membershipItemIter = (DBMembershipItem) iter.next();
        if (membershipItemIter.getType().equals(type)
            && membershipItemIter.getName().equals(name))
        {
        	membershipItem = membershipItemIter;
          break;
        }
      }
    }
    
    if (membershipItem == null || membershipItem.getPermissionLevel() == null){    	
    	PermissionLevel level = null;
    	//for groups awareness
    	if (type.equals(DBMembershipItem.TYPE_ROLE) || type.equals(DBMembershipItem.TYPE_GROUP))
      { 
    		
    		String levelName = null;
    		
    		if (membershipItem != null){
    			/** use level from stored item */
    			levelName = membershipItem.getPermissionLevelName();
    		}
    		else{    	
    			/** get level from config file */
    			levelName = ServerConfigurationService.getString(MC_DEFAULT
              + name);
    			    			
    			
    		}
      	        	
        if (levelName != null && levelName.trim().length() > 0)
        {
          level = permissionLevelManager.getPermissionLevelByName(levelName);
        } else if (name == null || ".anon".equals(name)) {
            level = permissionLevelManager.getDefaultNonePermissionLevel();
        } else{
        	Collection siteIds = new Vector();
        	siteIds.add(contextSiteId);        	
        	
        	if(type.equals(DBMembershipItem.TYPE_GROUP))
        	{
        	  level = permissionLevelManager.getDefaultNonePermissionLevel();
        	}else{
        		//check cache first:
        		Set allowedFunctions = null;
        		String cacheId = contextSiteId + "/" + name;
        		Object el = allowedFunctionsCache.get(cacheId);
        		if(el == null){
        			allowedFunctions = AuthzGroupService.getAllowedFunctions(name, siteIds);
        			allowedFunctionsCache.put(cacheId, allowedFunctions);
        		}else{
        			allowedFunctions = (Set) el;
        		}
        		if (allowedFunctions.contains(SiteService.SECURE_UPDATE_SITE)){        			        	        	
        			level = permissionLevelManager.getDefaultOwnerPermissionLevel();
        		}else{
        			level = permissionLevelManager.getDefaultContributorPermissionLevel();
        		}
        	}
        	
        }
      }
    	PermissionLevel noneLevel = permissionLevelManager.getDefaultNonePermissionLevel();
      membershipItem = new DBMembershipItemImpl();
      membershipItem.setName(name);
      membershipItem.setPermissionLevelName((level == null) ? noneLevel.getName() : level.getName() );
      membershipItem.setType(type);
      membershipItem.setPermissionLevel((level == null) ? noneLevel : level);      
    }        
    return membershipItem;
  }
  
  @Override
  public Attachment createDFAttachment(String attachId)
  {
    try
    {
      Attachment attach = messageManager.createAttachment();

      attach.setAttachmentId(attachId);

      attachmentService.initialise(attach);

      return attach;
    }
    catch (Exception e)
    {
		LOG.warn("Failed to create attachment: "+ attachId, e);
      return null;
    }
  }

	public List getDiscussionForumsWithTopics()
	{
    LOG.debug("getDiscussionForumsWithTopics()");
    return forumManager.getForumByTypeAndContextWithTopicsAllAttachments(typeManager
        .getDiscussionForumType());
	}
	
	public List getDiscussionForumsWithTopics(String contextId) {
	    if (LOG.isDebugEnabled()) LOG.debug("getDiscussionForumsWithTopics(String contextId)");
	    if (contextId == null) {
	        throw new IllegalArgumentException("Null contextId passed to getDiscussionForumsWithTopics");
	    }
	    String dfType = typeManager.getDiscussionForumType();
	    return forumManager.getForumByTypeAndContextWithTopicsAllAttachments(dfType, contextId);
	}

	public Map<Long, Boolean> getReadStatusForMessagesWithId(List<Long> msgIds, String userId)
	{
		LOG.debug("getDiscussionForumsWithTopics()");

		
		Map<Long, Boolean> msgIdStatusMap = new HashMap<Long, Boolean>();
		if (msgIds == null || msgIds.size() == 0) {
			LOG.debug("empty map returns b/c no msgIds passed to getReadStatusForMessagesWithId");
			return msgIdStatusMap;
		}

		if (userId == null) {
			LOG.debug("empty user assume that all messages are read");
			for (int i =0; i < msgIds.size(); i++) {
				msgIdStatusMap.put(msgIds.get(i), Boolean.valueOf(true));
			}
			return msgIdStatusMap; 
		}
		
		
		if (msgIds.size() < MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
			return messageManager.getReadStatusForMessagesWithId(msgIds, userId);
		} else {
			// if there are more than MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST msgs, we need to do multiple queries
			int begIndex = 0;
			int endIndex = 0;

			while (begIndex < msgIds.size()) {
				endIndex = begIndex + MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST;
				if (endIndex > msgIds.size()) {
					endIndex = msgIds.size();
				}
				List tempMsgIdList = new ArrayList();
				tempMsgIdList.addAll(msgIds.subList(begIndex, endIndex));
				Map<Long, Boolean> statusMap = messageManager.getReadStatusForMessagesWithId(tempMsgIdList, userId);
				msgIdStatusMap.putAll(statusMap);
				begIndex = endIndex;
			}
		}
		
		return msgIdStatusMap;
	}

	public List getDiscussionForumsWithTopicsMembershipNoAttachments(String contextId)
	{
    LOG.debug("getDiscussionForumsWithTopicsMembershipNoAttachments()");
    return forumManager.getForumByTypeAndContextWithTopicsMembership(typeManager
        .getDiscussionForumType(), contextId);
	}
	
	public List getPendingMsgsInTopic(Long topicId)
	{
		return messageManager.getPendingMsgsInTopic(topicId);
	}
	
	public int getNumModTopicsWithModPermissionByPermissionLevel(List membershipList)
	{
		return forumManager.getNumModTopicCurrentUserHasModPermForWithPermissionLevel(membershipList);
	}
	
	public int getNumModTopicsWithModPermissionByPermissionLevelName(List membershipList)
	{
		return forumManager.getNumModTopicCurrentUserHasModPermForWithPermissionLevelName(membershipList);
	}

    private String getEventMessage(Object object) {
    	String eventMessagePrefix = "";
    	final String toolId = ToolManager.getCurrentTool().getId();
    	
    		if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
    			eventMessagePrefix = "/messagesAndForums";
    		else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
    			eventMessagePrefix = "/messages";
    		else
    			eventMessagePrefix = "/forums";
    	
    	return eventMessagePrefix + getContextSiteId() + "/" + object.toString() + "/" + sessionManager.getCurrentSessionUserId();
    }

    public String getContextForTopicById(Long topicId) {
      return getTopicById(topicId).getOpenForum().getArea().getContextId();
    }

    public String getContextForForumById(Long forumId) {
      return getForumById(forumId).getArea().getContextId();
    }
    
    public String getContextForMessageById(Long messageId) {
      return getMessageById(messageId).getTopic().getOpenForum().getArea().getContextId();
    }

    public String ForumIdForMessage(Long messageId) {
      return getMessageById(messageId).getTopic().getOpenForum().getId().toString();
    }
    

    public Set<String> getUsersAllowedForTopic(Long topicId, boolean checkReadPermission, boolean checkModeratePermission) {
  	 LOG.debug("getUsersAllowedForTopic(" + topicId + ", " + checkReadPermission + ", " + checkModeratePermission + ")"); 
  	 
     if (topicId == null) {
  		  throw new IllegalArgumentException("Null topicId passed to getUsersAllowedToReadTopic");
  	  }
  	  
  	  Set<String> usersAllowed = new HashSet<String>();

  	  // we need to get all of the membership items associated with this topic

  	  // first, check to see if it is in the thread
  	  Set<DBMembershipItem> topicItems = new HashSet<DBMembershipItem>();
  	  DiscussionTopic topicWithMemberships = (DiscussionTopic)forumManager.getTopicByIdWithMemberships(topicId);
  	  if (topicWithMemberships != null && topicWithMemberships.getMembershipItemSet() != null) {
  		  topicItems = topicWithMemberships.getMembershipItemSet();
  	  }


  	  Set<Role> rolesInSite = null;
  	  Set<Group> groupsInSite = new HashSet<Group>();

  	  Site currentSite;
  	  String siteId = ToolManager.getCurrentPlacement().getContext();
  	  try {
  		  currentSite = siteService.getSite(siteId);

  		  // get all of the roles in this site
  		  rolesInSite = currentSite.getRoles();
  		  Collection<Group> groups = currentSite.getGroups();
  		  if (groups != null) {
  			  groupsInSite = new HashSet<Group>(groups);
  		  } 
  		  
  	  } catch (IdUnusedException iue) {
  		  LOG.warn("No site found with id: " + siteId + ". No users returned by getUsersAllowedToReadTopic");
  		  return new HashSet<String>();
  	  }
  	  
  	  List<DBMembershipItem> revisedMembershipItemSet = new ArrayList<DBMembershipItem>();
  	  // we need to get the membership items for the roles separately b/c of default permissions
  	  if (rolesInSite != null) {
  		  for (Role role : rolesInSite) {
  			  DBMembershipItem roleItem = getDBMember(topicItems, role.getId(), DBMembershipItem.TYPE_ROLE);
  			  if (roleItem != null) {
  				  revisedMembershipItemSet.add(roleItem);
  			  }
  		  }
  	  }
  	  // now add in the group perms
  	  for (Group group : groupsInSite) {
  		  DBMembershipItem groupItem = getDBMember(topicItems, group.getTitle(), DBMembershipItem.TYPE_GROUP);
  		  if (groupItem != null) {
  			  revisedMembershipItemSet.add(groupItem);
  		  }
  	  }
  	  
  	  // now we have the membership items. let's see which ones can read
  	  for (DBMembershipItem membershipItem : revisedMembershipItemSet) {
  		  if ((checkReadPermission && membershipItem.getPermissionLevel().getRead() && !checkModeratePermission) ||
  				  (!checkReadPermission && checkModeratePermission && membershipItem.getPermissionLevel().getModeratePostings()) ||
  				  (checkReadPermission && membershipItem.getPermissionLevel().getRead() && checkModeratePermission && membershipItem.getPermissionLevel().getModeratePostings())) {
  			  if (membershipItem.getType().equals(DBMembershipItem.TYPE_ROLE)) {
  				  // add the users who are a member of this role
  				  LOG.debug("Adding users in role: " + membershipItem.getName() + " with read: " + membershipItem.getPermissionLevel().getRead());
  				  Set<String> usersInRole = currentSite.getUsersHasRole(membershipItem.getName());
  				  usersAllowed.addAll(usersInRole);
  			  } else if (membershipItem.getType().equals(DBMembershipItem.TYPE_GROUP)) {
  				  String groupName = membershipItem.getName();
  				  for (Group group : groupsInSite) {
  					  if (group.getTitle().equals(groupName)) {
  						  Set<Member> groupMembers = group.getMembers();
  						  if (groupMembers != null) {
  							  for (Member member : groupMembers) {
  								  usersAllowed.add(member.getUserId());
  							  }
  						  }
  					  }
  				  }
  			  }
  		  }
  	  }
  		  
  	  return usersAllowed;
    }

	public MemoryService getMemoryService() {
		return memoryService;
	}

	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}
       
}
