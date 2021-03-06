package eu.bcvsolutions.idm.acc.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Entity on target system
 * 
 * @author Radek Tomiška
 *
 */
@Projection(name = "excerpt", types = SysSystemEntity.class)
public interface SysSystemEntityExcerpt extends AbstractDtoProjection {

	String getUid();

	SystemEntityType getEntityType();
	
	SysSystem getSystem();
}
