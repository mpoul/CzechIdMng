package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.json.DomainObjectReader;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.domain.PersistentEntityResolver;
import eu.bcvsolutions.idm.core.model.dto.BaseFilter;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.service.ReadWriteEntityService;

/**
 * CRUD operations
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 */
public abstract class AbstractReadWriteController<E extends BaseEntity, F extends BaseFilter> extends AbstractReadEntityController<E, F> {
	
	@Autowired
	private PersistentEntities persistentEntities; 
	
	@Autowired
	private Associations associationLinks;
	
	@Autowired
	private List<HttpMessageConverter<?>> messageConverters;
	
	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper mapper;
	
	// TODO: constructor with autowire instead lazy entityResolver will be better
	private PersistentEntityResolver entityResolver;
	
	public AbstractReadWriteController(ReadWriteEntityService<E, F> entityService) {
		super(entityService);
	}
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {		
		E createdIdentity = createEntity((E)getEntityResolver().resolveEntity(nativeRequest, getEntityClass(), null));
		return new ResponseEntity<>(toResource(createdIdentity, assembler), HttpStatus.CREATED);
	}
	
	public E createEntity(E entity) {
		// TODO: events
		return getEntityService().save(entity);
	}
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> update(
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		E updateEntity = getEntity(backendId);
		if (updateEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		E updatedEntity = updateEntity((E)getEntityResolver().resolveEntity(nativeRequest, getEntityService().getEntityClass(), updateEntity));
		return new ResponseEntity<>(toResource(updatedEntity, assembler), HttpStatus.OK);
	}
	
	public E updateEntity(E entity) {
		Assert.notNull(entity, "Entity is required");		
		return getEntityService().save(entity);
	}
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> patch(
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		E updateEntity = getEntity(backendId);
		if (updateEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		E updatedEntity = patchEntity((E)getEntityResolver().resolveEntity(nativeRequest, getEntityService().getEntityClass(), updateEntity));
		return new ResponseEntity<>(toResource(updatedEntity, assembler), HttpStatus.OK);
	}
	
	public E patchEntity(E entity) {
		Assert.notNull(entity, "Entity is required");
		return getEntityService().save(entity);
	}
	
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		E entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		deleteEntity(entity);
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	public void deleteEntity(E entity) {
		Assert.notNull(entity, "Entity is required");
		getEntityService().delete(entity);
	}
	
	protected ReadWriteEntityService<E, F> getEntityService() {
		return (ReadWriteEntityService<E, F>) super.getEntityService();
	}
	
	private PersistentEntityResolver getEntityResolver() {
		if (entityResolver == null) {
			entityResolver = new PersistentEntityResolver(messageConverters, new DomainObjectReader(persistentEntities, associationLinks));
		}
		return entityResolver;
	}

}