package org.sakaiproject.component.app.messageforums;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.RankImage;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.*;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Validator;

import javax.annotation.Resource;

/**
 * This handles attachments.
 * It's basically a class that hides the ContentHostingService.
 *
 * @author Matthew Buckett
 */
public class AttachmentService {

	private final Log log = LogFactory.getLog(AttachmentService.class);

	private ToolManager toolManager;
	private ContentHostingService contentHostingService;
	private UserDirectoryService userDirectoryService;


	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public String getAttachmentRelativeUrl(String id) {
		try
		{
			return contentHostingService.getResource(id).getUrl(true);
		}
		catch(Exception e)
		{
			log.error("MessageForumsMessageManagerImpl.getAttachmentUrl" + e, e);
		}
		return null;

	}

	public String getAttachmentUrl(String id)
	{
		try
		{
			return contentHostingService.getResource(id).getUrl(false);
		}
		catch(Exception e)
		{
			log.error("MessageForumsMessageManagerImpl.getAttachmentUrl" + e, e);
		}
		return null;
	}

	// TODO This should hide the Sakai exception
	// There is some other code around that uses the reference that gets passed back in the session to get all
	// the details about the attachment.
	public void initialise(Attachment attach) throws IdUnusedException, TypeException, PermissionException {
		// This is used to copy the data from content hosting into the attachment object when creating a
		// new attachment. The data should be in the attachment object so we don't have to go back to content
		// hosting for it.
		String attachId = attach.getAttachmentId();
		if (attachId == null) {
			throw new IllegalArgumentException("Attachment doesn't have an ID: "+ attach);
		}
		ContentResource cr = contentHostingService.getResource(attachId);

		attach.setAttachmentName(cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
		attach.setAttachmentSize((Long.valueOf(cr.getContentLength())).toString());
		attach.setCreatedBy(cr.getProperties().getProperty(
				cr.getProperties().getNamePropCreator()));
		attach.setModifiedBy(cr.getProperties().getProperty(
				cr.getProperties().getNamePropModifiedBy()));
		attach.setAttachmentType(cr.getContentType());

	}

	// TODO This should hide the Sakai exceptions
	public void initialise(RankImage attach) throws IdUnusedException, TypeException, PermissionException, UserNotDefinedException {

		ContentResource cr = contentHostingService.getResource(attach.getAttachmentId());

		User creator = userDirectoryService.getUser(cr.getProperties().getProperty(cr.getProperties().getNamePropCreator()));
		attach.setCreatedBy(creator.getDisplayName());
		User modifier = userDirectoryService.getUser(cr.getProperties().getProperty(cr.getProperties().getNamePropModifiedBy()));
		attach.setModifiedBy(modifier.getDisplayName());

		attach.setAttachmentSize((Long.valueOf(cr.getContentLength())).toString());
		attach.setAttachmentType(cr.getContentType());
		attach.setAttachmentUrl(resourceUrlEscaping(cr.getUrl()));
	}

	public void remove(String attachmentId) throws IdUnusedException, TypeException, InUseException, PermissionException {
		contentHostingService.removeResource(attachmentId);
	}

	public String duplicateAttachment(String attachmentId, String siteId) {
		try {
			ContentResource oldAttachment = contentHostingService.getResource(attachmentId);
			String title = toolManager.getTool("sakai.forums").getTitle();
			ContentResource attachment = contentHostingService.addAttachmentResource(
					oldAttachment.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME)
					, siteId, title, oldAttachment.getContentType(),
					oldAttachment.getContent(), oldAttachment.getProperties());
			return attachment.getId();
		} catch (SakaiException se) {
			// TODO Better exception?
			throw new RuntimeException("Failed to duplicate attachment: "+ attachmentId, se);
		}

	}

	/**
	 * Apparently, the ContentResource object gives a url, but it doesn't escape any special characters. So, need to do some
	 * escaping just for the name portion of the url. So, find the string "attachment" and escape anything after it.
	 */
	private String resourceUrlEscaping(String url) {
		int attIndex = url.indexOf("attachment");
		String leftOfAttachment = url.substring(0, attIndex);
		String rightOfAttachment = url.substring(attIndex);

		String finalUrl = leftOfAttachment.concat(Validator.escapeUrl(rightOfAttachment));
		return finalUrl;
	}

}

