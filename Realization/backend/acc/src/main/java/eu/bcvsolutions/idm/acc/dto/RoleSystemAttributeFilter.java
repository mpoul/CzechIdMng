package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for role system attribute mapping
 * 
 * @author svandav
 *
 */
public class RoleSystemAttributeFilter implements BaseFilter {

	private UUID roleSystemId;
	
	private Boolean isUid;

	public Boolean getIsUid() {
		return isUid;
	}

	public void setIsUid(Boolean isUid) {
		this.isUid = isUid;
	}

	public UUID getRoleSystemId() {
		return roleSystemId;
	}

	public void setRoleSystemId(UUID roleSystemId) {
		this.roleSystemId = roleSystemId;
	}
}
