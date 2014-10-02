package org.sakaiproject.component.app.messageforums;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.RankImage;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Validator;

/**
 * This handles attachments.
 * It's basically a class that hides the ContentHostingService.
 *
 * @author Matthew Buckett
 */
public class AttachmentService {

	private final Log log = LogFactory.getLog(AttachmentService.class);

	private ContentHostingService contentHostingService;
	private UserDirectoryService userDirectoryService;

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
	public void initialise(Attachment attach) throws IdUnusedException, TypeException, PermissionException {
		// This is used to copy the data from content hosting into the attachment object when creating a
		// new attachment. The data should be in the attachment object so we don't have to go back to content
		// hosting for it.
		String attachId = attach.getAttachmentId();
		if (attachId == null) {
			throw new IllegalArgumentException("Attachment doesn't have an ID: "+ attach);
		}
		ContentResource cr = contentHostingService.getResource(attachId);
		attach.setAttachmentSize((Long.valueOf(cr.getContentLength())).toString());
		attach.setCreatedBy(cr.getProperties().getProperty(
				cr.getProperties().getNamePropCreator()));
		attach.setModifiedBy(cr.getProperties().getProperty(
				cr.getProperties().getNamePropModifiedBy()));
		attach.setAttachmentType(cr.getContentType());

	}

	public void initalise(RankImage attach) throws IdUnusedException, TypeException, PermissionException, UserNotDefinedException {

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

