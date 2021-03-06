package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.security.api.domain.Enabled;

/**
 * Run provisioning after identity was saved.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
public class IdentitySaveProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentity> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentitySaveProvisioningProcessor.class);
	private SysProvisioningService provisioningService;
	private final ApplicationContext applicationContext;
	
	@Autowired
	public IdentitySaveProvisioningProcessor(ApplicationContext applicationContext) {
		super(CoreEventType.SAVE, CoreEventType.EAV_SAVE);
		//
		Assert.notNull(applicationContext);
		//
		this.applicationContext = applicationContext;
	}

	@Override
	public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		doProvisioning(event.getContent());
		return new DefaultEventResult<>(event, this);
	}
	
	private void doProvisioning(IdmIdentity identity) {
		LOG.debug("Call provisioning for idnetity [{}]", identity.getUsername());
		getProvisioningService().doProvisioning(identity);
	}
	
	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
	
	/**
	 * provisioningService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private SysProvisioningService getProvisioningService() {
		if (provisioningService == null) {
			provisioningService = applicationContext.getBean(SysProvisioningService.class);
		}
		return provisioningService;
	}
	
}