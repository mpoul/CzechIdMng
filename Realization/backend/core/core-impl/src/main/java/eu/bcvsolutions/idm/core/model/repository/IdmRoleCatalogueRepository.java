package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.dto.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.repository.projection.IdmRoleCatalogueExcerpt;

/**
 * Role catalogue reppository
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestResource( //
		collectionResourceRel = "roleCatalogues", // 
		path = "roleCatalogues", //
		itemResourceRel = "roleCatalogues", //
		excerptProjection = IdmRoleCatalogueExcerpt.class,
		exported = false,
		collectionResourceDescription = @Description("Role catalogues"))
public interface IdmRoleCatalogueRepository extends BaseRepository<IdmRoleCatalogue, RoleCatalogueFilter>{
	
	IdmRoleCatalogue findOneByName(@Param("name") String name);
	
	@Override
	@Query(value = "select e from IdmRoleCatalogue e" +
	        " where" +
	        " (?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})" + 
	        " and (?#{[0].parent} is null or e.parent = ?#{[0].parent})")
	Page<IdmRoleCatalogue> find(RoleCatalogueFilter filter, Pageable pageable);
	
	@Query(value = "select e from IdmRoleCatalogue e" +
	        " where" +
	        " e.parent.id = null")
	List<IdmRoleCatalogue> findRoots();
	
	@Query(value = "select e from IdmRoleCatalogue e" +
			" where" +
			" (:parent is null and e.parent.id IS NULL) or (e.parent.id = :parent)")
	List<IdmRoleCatalogue> findChildrenByParent(@Param(value = "parent") Long parent);
}