package eu.bcvsolutions.idm.eav.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteEntityController;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;

/**
 * EAV Form definitions
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
@RequestMapping(value = BaseEntityController.BASE_PATH + "/form-attributes")
public class IdmFormAttributeController extends DefaultReadWriteEntityController<IdmFormAttribute, EmptyFilter>  {

	@Autowired
	public IdmFormAttributeController(EntityLookupService entityLookupService) {
		super(entityLookupService);
	}
}
