package org.sakaiproject.tool.messageforums.entityproviders;

import java.util.*;

import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.*;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.messageforums.entityproviders.sparsepojos.*;
import org.sakaiproject.tool.messageforums.entityproviders.utils.MessageUtils;

/**
 * Provides the forums entity provider. 
 * 
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
public class ForumsEntityProviderImpl extends AbstractEntityProvider implements Outputable, AutoRegisterEntityProvider, ActionsExecutable, Describeable {
	
	private static final Log LOG = LogFactory.getLog(ForumsEntityProviderImpl.class);

	public final static String ENTITY_PREFIX = "forums";
	
	@Setter
	protected DiscussionForumManager forumManager;
	
	@Setter
	protected UIPermissionsManager uiPermissionsManager;
	
	@Setter
	protected SiteService siteService;
	
	@Setter
	protected ToolManager toolManager;

	@Setter
	private SecurityService securityService;

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	public String[] getHandledOutputFormats() {
		return new String[] {Formats.JSON,Formats.XML};
	}

	/**
	 * This handles the paths:
	 * 
	 * /direct/forums/site/SITEID.json
	 * /direct/forums/site/SITEID/forum/FORUMID.json
	 * /direct/forums/site/SITEID/forum/FORUMID/topic/TOPICID.json
     * /direct/forums/site/SITEID/forum/FORUMID/topic/TOPICID/message/MESSAGEID.json
	 * 
	 * @param view
	 * @param params
	 * @return
	 */
	@EntityCustomAction(action="site",viewKey=EntityView.VIEW_LIST)
    public Object handleSite(EntityView view, Map<String, Object> params) {
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("handleSite");
		}
		
		String userId = developerHelperService.getCurrentUserId();
		
		if(userId == null) {
			LOG.error("Not logged in");
			throw new EntityException("You must be logged in to retrieve fora.","",HttpServletResponse.SC_UNAUTHORIZED);
		}
		
        String siteId = view.getPathSegment(2);
        
        if(siteId == null) {
			LOG.error("Bad request. No SITEID supplied on path.");
        	throw new EntityException("Bad request: To get the fora in a site you need a url like '/direct/forum/site/SITEID.json'"
        									,"",HttpServletResponse.SC_BAD_REQUEST);
        }
        
        checkSiteAndToolAccess(siteId);
        
        String[] pathSegments = view.getPathSegments();
        
        if(pathSegments.length == 3) {
        	// This is a request for all the fora in the site
        	return getAllForaForSite(siteId,userId);
        } else if(pathSegments.length == 5) {
        	// This is a request for a particular forum in the site
        	Long forumId = -1L;
		
        	try {
        		forumId = Long.parseLong(view.getPathSegment(4));
        	} catch(NumberFormatException nfe) {
        		LOG.error("Bad request. FORUMID must be an integer.");
        		throw new EntityException("The forum id must be an integer.","",HttpServletResponse.SC_BAD_REQUEST);
        	}
        	return getForum(forumId,siteId,userId);
        } else if(pathSegments.length == 7) {
        	// This is a request for a particular topic in the forum
        	Long topicId = -1L;
		
        	try {
        		topicId = Long.parseLong(view.getPathSegment(6));
        	} catch(NumberFormatException nfe) {
        		LOG.error("Bad request. TOPICID must be an integer.");
        		throw new EntityException("The topic id must be an integer.","",HttpServletResponse.SC_BAD_REQUEST);
        	}
        	return getTopic(topicId,siteId,userId);
        } else if(pathSegments.length == 9) {
        	Long messageId = -1L;
    		
    		try {
    			messageId = Long.parseLong(view.getPathSegment(8));
    		} catch(NumberFormatException nfe) {
        		LOG.error("Bad request. MESSAGEID must be an integer.");
    			throw new EntityException("The message id must be an integer.","",HttpServletResponse.SC_BAD_REQUEST);
    		}
    		
    		return getMessage(messageId,siteId,userId);
        } else {
        	return null;
        }
	}
	
	private List<?> getAllForaForSite(String siteId,String userId) {
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("getAllForaForSite(" + siteId + "," + userId + ")");
		}
		
		List<SparsestForum> sparseFora = new ArrayList<SparsestForum>();
		
		List<DiscussionForum> fatFora = forumManager.getDiscussionForumsWithTopics(siteId);
		
		for(DiscussionForum fatForum : fatFora) {
			
			if( ! checkAccess(fatForum,userId)) {
				LOG.warn("Access denied for user id '" + userId + "' to forum '" + fatForum.getId()
							+ "'. This forum will not be returned.");
				continue;
			}
				
			List<Long> topicIds = new ArrayList<Long>();
			for(Topic topic : (List<Topic>) fatForum.getTopics()) {
				topicIds.add(topic.getId());
			}
			
			List<Object[]> topicTotals = forumManager.getMessageCountsForMainPage(topicIds);
			List<Object[]> topicReadTotals = forumManager.getReadMessageCountsForMainPage(topicIds);
		
			SparsestForum sparseForum = new SparsestForum(fatForum,developerHelperService);
			
			int totalForumMessages = 0;
			for(Object[] topicTotal : topicTotals) {
				totalForumMessages += (Integer) topicTotal[1];
			}
			sparseForum.setTotalMessages(totalForumMessages);
			
			int totalForumReadMessages = 0;
			for(Object[] topicReadTotal : topicReadTotals) {
				totalForumReadMessages += (Integer) topicReadTotal[1];
			}
			sparseForum.setReadMessages(totalForumReadMessages);
		
			sparseFora.add(sparseForum);
		}
		
		return sparseFora;
	}
	
	/**
	 * This will return a SparseForum populated down to the topics with their
	 * attachments.
	 */
	private Object getForum(Long forumId, String siteId, String userId) {
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("getForum(" + forumId + "," + siteId + "," + userId + ")");
		}
		
		DiscussionForum fatForum = forumManager.getForumByIdWithTopicsAttachmentsAndMessages(forumId);
		
		if(checkAccess(fatForum,userId)) {
			
			SparseForum sparseForum = new SparseForum(fatForum,developerHelperService);
			
			List<DiscussionTopic> fatTopics = (List<DiscussionTopic>) fatForum.getTopics();
			
			// Gather all the topic ids so we can make the minimum number
			// of calls for the message counts.
			List<Long> topicIds = new ArrayList<Long>();
			for(DiscussionTopic topic : fatTopics) {
				topicIds.add(topic.getId());
			}
				
			List<Object[]> topicTotals = forumManager.getMessageCountsForMainPage(topicIds);
			List<Object[]> topicReadTotals = forumManager.getReadMessageCountsForMainPage(topicIds);
			
			int totalForumMessages = 0;
			for(Object[] topicTotal : topicTotals) {
				totalForumMessages += (Integer) topicTotal[1];
			}
			sparseForum.setTotalMessages(totalForumMessages);
				
			int totalForumReadMessages = 0;
			for(Object[] topicReadTotal : topicReadTotals) {
				totalForumReadMessages += (Integer) topicReadTotal[1];
			}
			sparseForum.setReadMessages(totalForumReadMessages);
			
			// Reduce the fat topics to sparse topics while setting the total and read
			// counts. A SparseTopic will only be created if the currrent user has read access.
			List<SparsestTopic> sparseTopics = new ArrayList<SparsestTopic>();
			for(DiscussionTopic fatTopic : fatTopics) {
				
				// Only add this topic to the list if the current user has read permission
				if( ! uiPermissionsManager.isRead(fatTopic,fatForum,userId,siteId)) {
					// No read permission, skip this topic.
					continue;
				}
				
				SparsestTopic sparseTopic = new SparsestTopic(fatTopic);
				for(Object[] topicTotal : topicTotals) {
					if(topicTotal[0].equals(sparseTopic.getId())) {
						sparseTopic.setTotalMessages((Integer)topicTotal[1]);
					}
				}
				for(Object[] topicReadTotal : topicReadTotals) {
					if(topicReadTotal[0].equals(sparseTopic.getId())) {
						sparseTopic.setReadMessages((Integer)topicReadTotal[1]);
					}
				}
				
				List<SparseAttachment> attachments = new ArrayList<SparseAttachment>();
				for(Attachment attachment : (List<Attachment>) fatTopic.getAttachments()) {
					String url = developerHelperService.getServerURL() + "/access/content" + attachment.getAttachmentId();
					attachments.add(new SparseAttachment(attachment.getAttachmentName(),url));
				}
				sparseTopic.setAttachments(attachments);
				
				sparseTopics.add(sparseTopic);
			}
			
			sparseForum.setTopics(sparseTopics);
			
			return sparseForum;
		} else {
			LOG.error("Not authorised to access forum '" + forumId + "'");
			throw new EntityException("You are not authorised to access this forum.","",HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
	
	private Object getTopic(Long topicId,String siteId,String userId) {
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("getTopic(" + topicId + "," + siteId + "," + userId + ")");
		}
		
		// This call gets the attachments for the messages but not the topic. Unexpected, yes. Cool, not.
		DiscussionTopic fatTopic = (DiscussionTopic)forumManager.getTopicByIdWithMessagesAndAttachments(topicId);
		
		if(!uiPermissionsManager.isRead(topicId,fatTopic.getDraft(),false,userId,forumManager.getContextForTopicById(topicId))) {
			LOG.error("'" + userId + "' is not authorised to read topic '" + topicId + "'.");
			throw new EntityException("You are not authorised to read this topic.","",HttpServletResponse.SC_UNAUTHORIZED);
		}
		
		SparseTopic sparseTopic = new SparseTopic(fatTopic);
		
		// Setup the total and read message counts on the topic
		List<Long> topicIds = new ArrayList<Long>();
		topicIds.add(fatTopic.getId());
		
		List<Object[]> totalCounts = forumManager.getMessageCountsForMainPage(topicIds);
		if(totalCounts.size() > 0) {
			sparseTopic.setTotalMessages((Integer) totalCounts.get(0)[1]);
		} else {
			sparseTopic.setTotalMessages(0);
		}
		
		List<Object[]> readCounts = forumManager.getReadMessageCountsForMainPage(topicIds);
		if(readCounts.size() > 0) {
			sparseTopic.setReadMessages((Integer) readCounts.get(0)[1]);
		} else {
			sparseTopic.setReadMessages(0);
		}
		
		List<SparseMessage> messages = new ArrayList<SparseMessage>();
		for(Message fatMessage : (List<Message>) fatTopic.getMessages()) {
			SparseMessage sparseMessage = new SparseMessage(fatMessage,/* readStatus = */ false,/* addAttachments = */ true,developerHelperService.getServerURL());
			messages.add(sparseMessage);
		}
		
		List<SparseThread> threads = new MessageUtils().getThreadsWithCounts(messages, forumManager, userId);
		
		sparseTopic.setThreads(threads);
		
		return sparseTopic;
		
	}
	
	private Object getMessage(Long messageId,String siteId,String userId) {
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("getMessage(" + messageId + "," + siteId + "," + userId + ")");
		}
		
		Message fatMessage = forumManager.getMessageById(messageId);
		
		Topic fatTopic = forumManager.getTopicByIdWithMessagesAndAttachments(fatMessage.getTopic().getId());
		
		// This sets the attachments on the message.We have to do this as
        // getMessageById doesn't populate the attachments.
		setAttachments(fatMessage,fatTopic.getMessages());
		
		if(!uiPermissionsManager.isRead(fatTopic.getId(),((DiscussionTopic)fatTopic).getDraft(),false,userId,forumManager.getContextForTopicById(fatTopic.getId()))) {
			LOG.error("'" + userId + "' is not authorised to read message '" + messageId + "'.");
			throw new EntityException("You are not authorised to read this message.","",HttpServletResponse.SC_UNAUTHORIZED);
		}
		
		List<SparseMessage> messages = new ArrayList<SparseMessage>();
		
		for(Message fm : (List<Message>) fatTopic.getMessages()) {
			messages.add(new SparseMessage(fm,/* readStatus =*/ false,/* addAttachments =*/ true, developerHelperService.getServerURL()));
		}
		
		SparseMessage sparseThread = new SparseMessage(fatMessage,false,/* readStatus =*/ true,developerHelperService.getServerURL());
		
		new MessageUtils().attachReplies(sparseThread,messages, forumManager, userId);
		
		return sparseThread;
		
	}
	
	private boolean checkAccess(BaseForum baseForum, String userId) {
		
		if(baseForum instanceof OpenForum) {
			
			// If the supplied user is the super user, return true.
			if(securityService.isSuperUser(userId)) {
				return true;
			}
			
			OpenForum of = (OpenForum) baseForum;
			
			// If this is not a draft and is available, return true.
			if(!of.getDraft() && of.getAvailability()) {
				return true;
			}
			
			// If this is a draft/unavailable forum AND was authored by the current user, return true.
			if((of.getDraft() || !of.getAvailability()) && of.getCreatedBy().equals(userId)) {
				return true;
			}
		}
		else if(baseForum instanceof PrivateForum) {
			PrivateForum pf = (PrivateForum) baseForum;
			// If the current user is the creator, return true.
			if(pf.getCreatedBy().equals(userId)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * This is a dirty hack to set the attachments on the message. There doesn't seem
	 * to be an api for getting a single message with all attachments. If you try and retrieve
	 * them after, hibernate, wonderful framework that it is, throws a lazy exception.
	 * 
	 * @param unPopulatedMessage The message we want to set attachments on
	 * @param populatedMessages The list of populated messages retrieved from the forum manager
	 */
	private void setAttachments(Message unPopulatedMessage, List<Message> populatedMessages) {
		
		for(Message populatedMessage : populatedMessages) {
			if(populatedMessage.getId().equals(unPopulatedMessage.getId())
					&& populatedMessage.getHasAttachments()) {
				unPopulatedMessage.setAttachments(populatedMessage.getAttachments());
				break;
			}
		}
	}
	
	/**
	 * Checks whether the current user can access this site and whether they can
	 * see the forums tool.
	 * 
	 * @param siteId
	 * @throws EntityException
	 */
	private void checkSiteAndToolAccess(String siteId) throws EntityException {
        
		//check user can access this site
		Site site;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityException("Invalid siteId: " + siteId,"", HttpServletResponse.SC_BAD_REQUEST);
		} catch (PermissionException e) {
			throw new EntityException("No access to site: " + siteId,"",HttpServletResponse.SC_UNAUTHORIZED);
		}

		//check user can access the tool, it might be hidden
		ToolConfiguration toolConfig = site.getToolForCommonId("sakai.forums");
		if(!toolManager.isVisible(site, toolConfig)) {
			throw new EntityException("No access to tool in site: " + siteId, "",HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}
