package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.rest.projection.IdmTreeTypeExcerpt;

/**
 * Repository for tree types
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RepositoryRestResource(
		collectionResourceRel = "treeTypes",
		path = "tree-types",
		itemResourceRel = "treeType",
		collectionResourceDescription = @Description("Tree types"),
		itemResourceDescription = @Description("Tree types"),
		excerptProjection = IdmTreeTypeExcerpt.class,
		exported = false
	)
public interface IdmTreeTypeRepository extends AbstractEntityRepository<IdmTreeType, QuickFilter> {
	
	IdmTreeType findOneByCode(@Param("code") String code);
	
	@Override
	@Query(value = "select e from IdmTreeType e" +
	        " where"
	        + " ("
		        + " ?#{[0].text} is null"
		        + " or lower(e.code) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
		        + " or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        + " )")
	Page<IdmTreeType> find(QuickFilter filter, Pageable pageable);
}
