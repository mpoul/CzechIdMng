package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.data.rest.core.support.EntityLookup;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleLookup;
import eu.bcvsolutions.idm.core.model.repository.processor.RevisionAssembler;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;
import eu.bcvsolutions.idm.core.model.service.IdmRoleService;
import eu.bcvsolutions.idm.core.rest.BaseEntityController;

/**
 * IdmRole endpoint
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/roles")
public class IdmRoleController extends DefaultReadWriteEntityController<IdmRole, QuickFilter> {

	@Autowired
	private IdmRoleLookup roleLookup;
	
	@Autowired
	private IdmAuditService auditService; 
	
	@Autowired
	public IdmRoleController(IdmRoleService roleService) {
		super(roleService);
	}
	
	@Override
	protected EntityLookup<IdmRole> getEntityLookup() {
		return roleLookup;
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_WRITE + "')")
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_WRITE + "')")
	public ResponseEntity<?> update(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_WRITE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "{roleId}/revisions/{revId}", method = RequestMethod.GET)
	public ResponseEntity<ResourceWrapper<DefaultRevisionEntity>> findRevision(@PathVariable("roleId") String roleId, @PathVariable("revId") Integer revId) {
		IdmRole originalEntity = (IdmRole)this.roleLookup.lookupEntity(roleId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("role", roleId));
		}
		
		Revision<Integer, ? extends AbstractEntity> revision;
		try {
			revision = this.auditService.findRevision(IdmRole.class, revId, originalEntity.getId());
		} catch (RevisionDoesNotExistException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", roleId));
		}
		
		IdmRole entity = (IdmRole) revision.getEntity();
		RevisionAssembler<IdmRole> assembler = new RevisionAssembler<IdmRole>();
		ResourceWrapper<DefaultRevisionEntity> resource = assembler.toResource(this.getClass(),
				String.valueOf(this.roleLookup.getResourceIdentifier(entity)), revision, revId);

		return new ResponseEntity<ResourceWrapper<DefaultRevisionEntity>>(resource, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "{roleId}/revisions", method = RequestMethod.GET)
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>> findRevisions(@PathVariable("roleId") String roleId) {
		IdmRole originalEntity = (IdmRole) this.roleLookup.lookupEntity(roleId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("role", roleId));
		}
		
		List<ResourceWrapper<DefaultRevisionEntity>> wrappers = new ArrayList<>();
		List<Revision<Integer, ? extends AbstractEntity>> results;
		try {
			 results = this.auditService.findRevisions(IdmRole.class, originalEntity.getId());
		} catch (RevisionDoesNotExistException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", roleId));
		}
		
		RevisionAssembler<IdmRole> assembler = new RevisionAssembler<IdmRole>();
		
		for	(Revision<Integer, ? extends AbstractEntity> revision : results) {
			wrappers.add(assembler.toResource(this.getClass(), 
					String.valueOf(this.roleLookup.getResourceIdentifier((IdmRole)revision.getEntity())),
					revision, revision.getRevisionNumber()));
		}
		
		ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>> resources = new ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>(
				wrappers);
		
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>>(resources, HttpStatus.OK);
	}
	
	@Override
	protected QuickFilter toFilter(MultiValueMap<String, Object> parameters) {
		QuickFilter filter = new QuickFilter();
		filter.setText((String)parameters.toSingleValueMap().get("text"));
		return filter;
	}
}