/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.api.app.messageforums.Rank;
import org.sakaiproject.api.app.messageforums.RankImage;
import org.sakaiproject.api.app.messageforums.RankManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.RankImageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.RankImpl;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class RankManagerImpl extends HibernateDaoSupport implements RankManager {
    private static final Log LOG = LogFactory.getLog(RankManagerImpl.class);
    private static final String QUERY_BY_CONTEXT_ID_USERID = "findRanksByContextIdUserID";
    private static final String QUERY_BY_CONTEXT_ID_NUM_POSTS_BASED = "findRanksByContextIdBasedOnNumPost";
    private static final String QUERY_BY_CONTEXT_ID = "findRanksByContextId";
    private static final String QUERY_BY_CONTEXT_ID_ORDER_BY_MIN_POST_DESC = "findRanksByContextIdOrderByMinPostDesc";
    private static final String QUERY_BY_RANK_ID = "findRankByRankId";

    private IdManager idManager;

    private SessionManager sessionManager;

    protected UserDirectoryService userDirectoryService;

    private EventTrackingService eventTrackingService;

    private AttachmentService attachmentService;

    public void init() {
        LOG.info("init()");
    }

    public EventTrackingService getEventTrackingService() {
        return eventTrackingService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

	public void setAttachmentService(AttachmentService attachmentService) {
		this.attachmentService = attachmentService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    private String getContextId() {
        if (TestUtil.isRunningTests()) {
            return "test-context";
        }
        Placement placement = ToolManager.getCurrentPlacement();
        String presentSiteId = placement.getContext();
        return presentSiteId;
    }

    public void saveRank(Rank rank) {
        rank.setUuid(getNextUuid());
        rank.setCreated(new Date());
        rank.setCreatedBy(getCurrentUser());
        rank.setModified(new Date());
        rank.setModifiedBy(getCurrentUser());
        rank.setContextId(getContextId());
        getHibernateTemplate().saveOrUpdate(rank);
        if (LOG.isDebugEnabled()) LOG.debug("saveRank executed for rank = " + rank.getTitle() + " contextid = " + rank.getContextId());
    }

    public List getRankList(final String contextId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRank(contextId: " + contextId + ")");
        }
        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_CONTEXT_ID);
                q.setParameter("contextId", contextId, Hibernate.STRING);
                return q.list();
            }
        };

        List ranklist = (List) getHibernateTemplate().execute(hcb);
        return ranklist;
    }

    public List findRanksByContextIdOrderByMinPostDesc(final String contextId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRank(contextId: " + contextId + ")");
        }

        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_CONTEXT_ID_ORDER_BY_MIN_POST_DESC);
                q.setParameter("contextId", contextId, Hibernate.STRING);
                return q.list();
            }
        };

        List ranklist = (List) getHibernateTemplate().execute(hcb);
        return ranklist;
    }

    private String getCurrentUser() {
        if (TestUtil.isRunningTests()) {
            return "test-user";
        }
        return sessionManager.getCurrentSessionUserId();
    }

    private String getNextUuid() {
        return idManager.createUuid();
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    public IdManager getIdManager() {
        return idManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public RankImage createRankImage() {
        RankImage image = new RankImageImpl();
        image.setUuid(getNextUuid());
        image.setCreated(new Date());
        image.setCreatedBy(getCurrentUser());
        image.setModified(new Date());
        image.setModifiedBy(getCurrentUser());

        LOG.info("createRankImage:  Rank Image  " + image.getUuid() + " created successfully");
        return image;
    }

    public void removeRank(Rank rank) {
        LOG.info("removeRank(Rank rank)");
        if (rank.getRankImage() != null) {
            removeImageAttachmentObject(rank.getRankImage());
        }
        getHibernateTemplate().delete(rank);
    }

    public void removeImageAttachmentObject(RankImage o) {
        LOG.info("removeImageAttachmentObject(RankImage o)");
        getHibernateTemplate().delete(o);
    }

    public void removeImageAttachToRank(final Rank rank, final RankImage imageAttach) {
        LOG.info("removeImageAttachToRank(final Rank rank, final RankImage imageAttach)");
        if (rank == null || imageAttach == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Rank returnedData = (Rank) session.get(RankImpl.class, rank.getId());
                RankImage returnedAttach = (RankImage) session.get(RankImageImpl.class, Long.valueOf(imageAttach.getId()));
                if (returnedData != null) {
                    returnedData.setRankImage(null);
                    session.saveOrUpdate(returnedData);

                    if (returnedAttach.getAttachmentId().toLowerCase().startsWith("/attachment"))
                        try {
                            attachmentService.remove(returnedAttach.getAttachmentId());
                            session.delete(returnedAttach);
                        } catch (PermissionException e) {
                            e.printStackTrace();
                        } catch (IdUnusedException e) {
                            e.printStackTrace();
                        } catch (TypeException e) {
                            e.printStackTrace();
                        } catch (InUseException e) {
                            e.printStackTrace();
                        }
                }
                return null;
            }
        };
        getHibernateTemplate().execute(hcb);
    }

    public Rank getRankById(final Long rankId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRankById: " + rankId + ")");
        }

        if (rankId == null) {
            throw new IllegalArgumentException("getRankById(): rankId is null");
        }

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_RANK_ID);
                q.setParameter("rankId", rankId, Hibernate.LONG);
                return q.uniqueResult();
            }
        };

        Rank rank = (Rank) getHibernateTemplate().execute(hcb);
        return rank;
    }

    public RankImage createRankImageAttachmentObject(String attachId, String name) {
        try {
            RankImage attach = new RankImageImpl();
            attach.setCreated(new Date());
            attach.setModified(new Date());
            attach.setAttachmentId(attachId);
            attach.setAttachmentName(name);
			attachmentService.initialise(attach);
            getHibernateTemplate().saveOrUpdate(attach);

            return attach;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addImageAttachToRank(final Rank rank, final RankImage imageAttach) {

        if (rank == null || imageAttach == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Rank returnedData = (Rank) session.get(RankImpl.class, rank.getId());
                if (returnedData != null) {
                    imageAttach.setRank(rank);
                    returnedData.setRankImage(imageAttach);
                    session.save(returnedData);
                }
                return null;
            }
        };
        getHibernateTemplate().execute(hcb);
    }


    public List findRanksByContextIdUserId(final String contextId, final String userid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findRanksByContextIdBasedOnRoles(contextId: " + contextId + ")");
        }

        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        String userEid = null;
        
        try {
			userEid = userDirectoryService.getUserEid(userid);
		} catch (UserNotDefinedException e) {
			throw new IllegalArgumentException("Cannot find user with id " + userid);
		}
        
        final String fUserEid = userEid;

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_CONTEXT_ID_USERID);
                q.setParameter("contextId", contextId, Hibernate.STRING);
                q.setParameter("userId", fUserEid, Hibernate.STRING);
                return q.list();
            }
        };

        List ranklist = (List) getHibernateTemplate().execute(hcb);
        return ranklist;
    }

    public List findRanksByContextIdBasedOnNumPost(final String contextId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findRanksByContextIdBasedOnNumPost(contextId: " + contextId + ")");
        }

        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_CONTEXT_ID_NUM_POSTS_BASED);
                q.setParameter("contextId", contextId, Hibernate.STRING);
                return q.list();
            }
        };

        List ranklist = (List) getHibernateTemplate().execute(hcb);
        return ranklist;
    }
}
