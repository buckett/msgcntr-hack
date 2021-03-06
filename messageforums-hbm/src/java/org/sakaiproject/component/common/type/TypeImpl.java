/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/common/trunk/impl/src/java/org/sakaiproject/component/common/type/TypeImpl.java $
 * $Id: TypeImpl.java 105077 2012-02-24 22:54:29Z ottenhoff@longsight.com $
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

package org.sakaiproject.component.common.type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.component.common.manager.PersistableImpl;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public class TypeImpl extends PersistableImpl implements Type
{
	private String authority;

	private String domain;

	private String keyword;

	private String description;

	private String displayName;

	/**
	 * Simple pattern for implementing a businessKey.
	 * 
	 * @return
	 */
	private String getBusinessKey()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(authority);
		sb.append(domain);
		sb.append(keyword);
		return sb.toString();
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof Type)) return false;
		if (obj instanceof TypeImpl)
		{ // found well known Type
			TypeImpl other = (TypeImpl) obj;
			if (this.getBusinessKey().equals(other.getBusinessKey())) return true;
		}
		else
		{ // found external Type
			Type other = (Type) obj;
			if (this.getAuthority().equals(other.getAuthority()) && this.getDomain().equals(other.getDomain())
					&& this.getKeyword().equals(other.getKeyword())) return true;
		}
		return false;
	}

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode()
	{
		return getBusinessKey().hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{id=");
		sb.append(super.id);
		sb.append(", displayName=");
		sb.append(displayName);
		sb.append(", authority=");
		sb.append(authority);
		sb.append(", domain=");
		sb.append(domain);
		sb.append(", keyword=");
		sb.append(keyword);
		sb.append("}");
		return sb.toString();
	}

	public String getAuthority()
	{
		return authority;
	}

	public String getDomain()
	{
		return domain;
	}

	public String getKeyword()
	{
		return keyword;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getDescription()
	{
		return description;
	}

	public void setAuthority(String authority)
	{
		if (authority == null || authority.length() < 1) throw new IllegalArgumentException("authority");

		this.authority = authority;
	}

	public void setDomain(String domain)
	{
		if (domain == null || domain.length() < 1) throw new IllegalArgumentException("domain");

		this.domain = domain;
	}

	public void setKeyword(String keyword)
	{
		if (keyword == null || keyword.length() < 1) throw new IllegalArgumentException("keyword");

		this.keyword = keyword;
	}

	public void setDisplayName(String displayName)
	{
		if (displayName == null || displayName.length() < 1) throw new IllegalArgumentException("displayName");

		this.displayName = displayName;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

}
