package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/**
 * Operations with identity roles - usable in wf
 * 
 * @author svanda
 *
 */
public interface IdmIdentityRoleService extends ReadWriteEntityService<IdmIdentityRole, IdentityRoleFilter> {
	
	/**
	 * Returns all identity's roles
	 * 
	 * @param identity
	 * @return
	 */
	List<IdmIdentityRole> getRoles(IdmIdentity identity);
	
	/**
	 * Returns identity roles by their ids (uuid in string).
	 * 
	 * @param ids
	 * @return
	 */
	List<IdmIdentityRole> getByIds(List<String> ids);

	IdmIdentityRole updateByDto(String id, IdmIdentityRoleDto dto);

	IdmIdentityRole addByDto(IdmIdentityRoleDto dto);
}
