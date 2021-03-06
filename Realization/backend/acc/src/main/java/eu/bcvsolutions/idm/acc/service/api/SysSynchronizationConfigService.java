package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationConfig;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Synchronization config service
 * @author svandav
 *
 */
public interface SysSynchronizationConfigService extends ReadWriteEntityService<SysSynchronizationConfig, SynchronizationConfigFilter> {

}
