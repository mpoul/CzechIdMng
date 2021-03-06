package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import org.springframework.data.rest.core.support.EntityLookup;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Support for loading {@link BaseEntity} by identifier.
 * Provide basic services through application.
 * 
 * @see {@link EntityLookup}
 * @see {@link ReadEntityService}
 * 
 * @author Radek Tomiška
 *
 */
public interface EntityLookupService {

	/**
	 * Returns entity
	 * 
	 * @param entityClass
	 * @param entityId
	 * @param <E> {@link BaseEntity} type
	 * @return
	 */
	<E extends BaseEntity> E lookup(Class<E> entityClass, Serializable entityId);
	
	/**
	 * Returns {@link EntityLookup} for given entityClass
	 * 
	 * @param entityClass
	 * @param <E> {@link BaseEntity} type
	 * @return
	 */
	<E extends BaseEntity> EntityLookup<E> getEntityLookup(Class<E> entityClass);
	
	/**
	 * Returns base service for given entity
	 * 
	 * @param entityClass
	 * @param <E> {@link BaseEntity} type
	 * @return
	 */
	<E extends BaseEntity> ReadEntityService<E, ?> getEntityService(Class<E> entityClass);
	
	/**
	 * Returns base service for given entity in given type
	 * 
	 * @param entityClass
	 * @param entityServiceClass
	 * @param <E> {@link BaseEntity} type
	 * @param <S> {@link ReadEntityService} type
	 * @return
	 */
	<E extends BaseEntity, S extends ReadEntityService<E, ?>> S getEntityService(Class<E> entityClass, Class<S> entityServiceClass);
}
