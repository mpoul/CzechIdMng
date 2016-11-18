package eu.bcvsolutions.idm.eav.service.api;

import java.util.List;
import java.util.UUID;

import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.repository.AbstractFormValueRepository;

/**
 * Custom form value service can be registered by spring plugin
 * 
 * @author Radek Tomiška
 *
 * @param <O> values owner
 * @param <E> values entity
 * @param <F> filter
 */
public abstract class AbstractFormValueService<O extends FormableEntity, E extends AbstractFormValue<O>> implements FormValueService<O, E> {

	private final Class<O> ownerClass;

	@SuppressWarnings("unchecked")
	public AbstractFormValueService() {
		ownerClass = (Class<O>) GenericTypeResolver.resolveTypeArguments(getClass(), AbstractFormValueService.class)[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return ownerClass.isAssignableFrom(delimiter);
	}
	
	protected abstract AbstractFormValueRepository<O, E> getRepository();
	
	/**
	 * Returns entity by given id. Returns null, if entity is not exists. For AbstractEntity uuid or string could be given.
	 */
	@Override
	@Transactional(readOnly = true)
	public E get(UUID id) {
		return getRepository().findOne(id);
	}
	
	/**
	 * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
	 * entity instance completely.
	 * 
	 * @param entity
	 * @return the saved entity
	 */
	@Override
	@Transactional
	public E save(E entity) {
		return getRepository().save(entity);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<E> getValues(O owner, IdmFormDefinition formDefiniton) {
		//
		if (formDefiniton == null) {
			return Lists.newArrayList(getRepository().findByOwner(owner));
		}
		return getRepository().findByOwnerAndFormAttribute_FormDefinition(owner, formDefiniton);
	}
	
	@Transactional
	public void deleteValues(O owner, IdmFormDefinition formDefiniton) {
		getRepository().delete(getValues(owner, formDefiniton));
	}
	
	

}