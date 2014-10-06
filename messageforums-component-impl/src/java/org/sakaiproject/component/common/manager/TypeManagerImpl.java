/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/common/trunk/common-composite-component/src/java/org/sakaiproject/component/common/type/TypeManagerImpl.java $
 * $Id: TypeManagerImpl.java 125281 2013-05-31 03:42:46Z nbotimer@unicon.net $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.component.common.manager;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.api.common.type.TypeManager;
import org.sakaiproject.component.common.type.TypeImpl;
import org.sakaiproject.id.api.IdManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.sql.SQLException;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public class TypeManagerImpl extends HibernateDaoSupport implements TypeManager
{
	private static final String UUID = "uuid";

	private static final String FIND_TYPE_BY_UUID = "findTypeByUuid";

	private static final String FIND_TYPE_BY_TUPLE = "findTypeByTuple";

	private static final String AUTHORITY = "authority";

	private static final String DOMAIN = "domain";

	private static final String KEYWORD = "keyword";


	private boolean cacheFindTypeByTuple = true;

	private boolean cacheFindTypeByUuid = true;

	private PersistableHelper persistableHelper; // dep inj

	private IdManager idManager;


	public Type createType(String authority, String domain, String keyword, String displayName, String description)
	{
		// validation
		if (authority == null || authority.length() < 1 || authority.length() > 100)
			throw new IllegalArgumentException("authority");
		if (domain == null || domain.length() < 1 || domain.length() > 100)
			throw new IllegalArgumentException("domain");
		if (keyword == null || keyword.length() < 1 || keyword.length() > 100)
			throw new IllegalArgumentException("keyword");
		if (displayName == null || displayName.length() < 1| displayName.length() > 255)
			throw new IllegalArgumentException("displayName");

		TypeImpl ti = new TypeImpl();
		persistableHelper.createPersistableFields(ti);
		ti.setUuid(idManager.createUuid());
		ti.setAuthority(authority);
		ti.setDomain(domain);
		ti.setKeyword(keyword);
		ti.setDisplayName(displayName);
		ti.setDescription(description);
		getHibernateTemplate().save(ti);
		return ti;
	}

	public void saveType(Type type)
	{
		if (type == null) throw new IllegalArgumentException("type");

		if (type instanceof TypeImpl)
		{ // found well known Type
			TypeImpl ti = (TypeImpl) type;
			persistableHelper.modifyPersistableFields(ti);
			getHibernateTemplate().saveOrUpdate(ti);
		}
		else
		{ // found external Type
			throw new IllegalArgumentException("Alternate Type implementations not supported.");
		}
	}

	public Type getType(final String uuid)
	{
		if (uuid == null || uuid.length() < 1)
		{
			throw new IllegalArgumentException("uuid");
		}

		final HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query q = session.getNamedQuery(FIND_TYPE_BY_UUID);
				q.setString(UUID, uuid);
				q.setCacheable(cacheFindTypeByUuid);
				q.setCacheRegion(Type.class.getCanonicalName());
				return q.uniqueResult();
			}
		};
		Type type = (Type) getHibernateTemplate().execute(hcb);
		return type;
	}

	public Type getType(final String authority, final String domain, final String keyword)
	{
		// validation
		if (authority == null || authority.length() < 1) throw new IllegalArgumentException("authority");
		if (domain == null || domain.length() < 1) throw new IllegalArgumentException("domain");
		if (keyword == null || keyword.length() < 1) throw new IllegalArgumentException("keyword");

		final HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query q = session.getNamedQuery(FIND_TYPE_BY_TUPLE);
				q.setString(AUTHORITY, authority);
				q.setString(DOMAIN, domain);
				q.setString(KEYWORD, keyword);
				q.setCacheable(cacheFindTypeByTuple);
				q.setCacheRegion(Type.class.getCanonicalName());
				return q.uniqueResult();
			}
		};
		Type type = (Type) getHibernateTemplate().execute(hcb);
		return type;
	}

	/**
	 * @param cacheFindTypeByTuple
	 *        The cacheFindTypeByTuple to set.
	 */
	public void setCacheFindTypeByTuple(boolean cacheFindTypeByTuple)
	{
		this.cacheFindTypeByTuple = cacheFindTypeByTuple;
	}

	/**
	 * @param cacheFindTypeByUuid
	 *        The cacheFindTypeByUuid to set.
	 */
	public void setCacheFindTypeByUuid(boolean cacheFindTypeByUuid)
	{
		this.cacheFindTypeByUuid = cacheFindTypeByUuid;
	}

	public void deleteType(Type type)
	{
		throw new UnsupportedOperationException("Types should never be deleted!");
	}

	/**
	 * @param persistableHelper
	 *        The persistableHelper to set.
	 */
	public void setPersistableHelper(PersistableHelper persistableHelper)
	{
		this.persistableHelper = persistableHelper;
	}

	public void setIdManager(IdManager idManager) {
		this.idManager = idManager;
	}
}
