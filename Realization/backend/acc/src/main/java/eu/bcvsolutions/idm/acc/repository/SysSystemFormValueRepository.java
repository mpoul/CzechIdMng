package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.eav.repository.AbstractFormValueRepository;

/**
 * Extended attributes for target system configuration
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(exported = false)
public interface SysSystemFormValueRepository extends AbstractFormValueRepository<SysSystem, SysSystemFormValue> {
	
}
