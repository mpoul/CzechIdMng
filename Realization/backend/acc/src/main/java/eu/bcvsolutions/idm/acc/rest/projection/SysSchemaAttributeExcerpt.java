package eu.bcvsolutions.idm.acc.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Schema attribute excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSchemaAttribute.class)
public interface SysSchemaAttributeExcerpt extends AbstractDtoProjection {
	
	String getName();

	String getClassType();
	
	boolean isRequired();
	
	boolean isMultivalued();
	
	SysSchemaObjectClass getObjectClass();
}
