package eu.bcvsolutions.idm.core.model.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import eu.bcvsolutions.idm.core.model.dto.BaseFilter;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;

/**
 * Provide additional methods to retrieve entities using the pagination and
 * sorting abstraction.
 * 
 * @author Radek Tomiška
 * @see Sort
 * @see Pageable
 * @see Page
 */
public interface ReadEntityService<E extends BaseEntity, F extends BaseFilter> extends BaseEntityService<E> {

	/**
	 * Returns entity by given id. Returns null, if entity is not exists
	 * 
	 * @param id
	 * @return
	 */
	E get(Long id);
	
	/**
	 * Returns page of entities
	 * 
	 * @param pageable
	 * @return
	 */
	Page<E> find(Pageable pageable);
	
	/**
	 * Returns page of entities by given filter
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 */
	Page<E> find(F filter, Pageable pageable);
}