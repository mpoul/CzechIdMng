package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationLog;
import eu.bcvsolutions.idm.acc.rest.projection.SysSynchronizationLogExcerpt;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Synchronization log repository
 * 
 * @author Svanda
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "synchronizationLogs", //
		path = "synchronization-logs", //
		itemResourceRel = "synchronizationLog", //
		excerptProjection = SysSynchronizationLogExcerpt.class,
		exported = false // we are using repository metadata, but we want expose
							// rest endpoint manually
)
public interface SysSynchronizationLogRepository extends AbstractEntityRepository<SysSynchronizationLog, SynchronizationLogFilter> {

	@Override
	@Query(value = "select e from SysSynchronizationLog e"+ 
			" where" +
	        " (?#{[0].synchronizationConfigId} is null or e.synchronizationConfig.id = ?#{[0].synchronizationConfigId})"
			)
	Page<SysSynchronizationLog> find(SynchronizationLogFilter filter, Pageable pageable);
}
