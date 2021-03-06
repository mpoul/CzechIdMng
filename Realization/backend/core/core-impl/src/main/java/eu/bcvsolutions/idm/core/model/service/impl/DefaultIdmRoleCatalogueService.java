package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueService;

/**
 * Implementation of @IdmRoleCatalogueService
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultIdmRoleCatalogueService extends AbstractReadWriteEntityService<IdmRoleCatalogue, RoleCatalogueFilter>  implements IdmRoleCatalogueService {
	
	private final IdmRoleCatalogueRepository roleCatalogueRepository;
	private final IdmRoleRepository roleRepository;
	private final DefaultBaseTreeService<IdmRoleCatalogue> baseTreeService;
	
	@Autowired
	public DefaultIdmRoleCatalogueService(
			IdmRoleCatalogueRepository roleCatalogueRepository,
			IdmRoleRepository roleRepository,
			DefaultBaseTreeService<IdmRoleCatalogue> baseTreeService) {
		super(roleCatalogueRepository);
		//
		Assert.notNull(roleRepository);
		Assert.notNull(baseTreeService);
		//
		this.roleCatalogueRepository = roleCatalogueRepository;
		this.roleRepository = roleRepository;
		this.baseTreeService = baseTreeService;
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmRoleCatalogue getByName(String name) {
		return roleCatalogueRepository.findOneByName(name);
	}
	
	@Override
	public IdmRoleCatalogue save(IdmRoleCatalogue entity) {
		// test role catalogue to parent and children
		if (this.baseTreeService.validateTreeNodeParents(entity)) {
			throw new TreeNodeException(CoreResultCode.ROLE_CATALOGUE_BAD_PARENT,  "Role catalog ["+entity.getName() +"] have bad parent.");
		}
		return super.save(entity);
	}
	
	@Override
	@Transactional
	public void delete(IdmRoleCatalogue roleCatalogue) {
		Assert.notNull(roleCatalogue);
		//
		if (!findChildrenByParent(roleCatalogue.getId()).isEmpty()) {
			throw new ResultCodeException(CoreResultCode.ROLE_CATALOGUE_DELETE_FAILED_HAS_CHILDREN, ImmutableMap.of("roleCatalogue", roleCatalogue.getName()));
		}
		// selected role catalogues - set to null
		roleRepository.clearCatalogue(roleCatalogue);
		//
		super.delete(roleCatalogue);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogue> findRoots() {
		return this.roleCatalogueRepository.findChildren(null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogue> findChildrenByParent(UUID parent) {
		return this.roleCatalogueRepository.findChildren(parent);
	}
	
}
