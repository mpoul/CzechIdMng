package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.rest.projection.SysSystemEntityExcerpt;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Entity on target system
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "systemEntities", //
		path = "system-entities", //
		itemResourceRel = "systemEntity", //
		excerptProjection = SysSystemEntityExcerpt.class,
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface SysSystemEntityRepository extends AbstractEntityRepository<SysSystemEntity, SystemEntityFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from SysSystemEntity e" +
	        " where" +
	        " (?#{[0].systemId} is null or e.system.id = ?#{[0].systemId})" +
	        " and" +
	        " (lower(e.uid) like ?#{[0].uid == null ? '%' : '%'.concat([0].uid.toLowerCase()).concat('%')})" +
	        " and" + 
	        " (?#{[0].entityType} is null or e.entityType = ?#{[0].entityType})")
	Page<SysSystemEntity> find(SystemEntityFilter filter, Pageable pageable);
	
	SysSystem findOneByUidAndEntityType(@Param("uid") String uid, @Param("entityType") SystemEntityType entityType);
	
	Long countBySystem(@Param("system") SysSystem system);	
}
