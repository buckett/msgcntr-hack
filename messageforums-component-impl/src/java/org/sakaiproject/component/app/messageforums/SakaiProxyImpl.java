package org.sakaiproject.component.app.messageforums;

import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Matthew Buckett
 */
@Service("org.sakaiproject.component.app.messageforums.SakaiProxy")
public class SakaiProxyImpl implements SakaiProxy {

	private SessionManager sessionManager;

	private IdManager idManager;

	private ToolManager toolManager;

	@Inject
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Inject
	public void setIdManager(IdManager idManager) {
		this.idManager = idManager;
	}

	@Inject
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	@Override
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	@Override
	public String getUuid() {
		return idManager.createUuid();
	}

	@Override
	public String getCurrentSiteId() {
		return toolManager.getCurrentPlacement().getContext();
	}

	@Override
	public String getToolTitle(String siteId, String toolId) {
		return toolManager.getTool(toolId).getTitle();
	}
}
