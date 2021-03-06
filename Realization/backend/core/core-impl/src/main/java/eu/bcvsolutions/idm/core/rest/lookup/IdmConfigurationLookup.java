package eu.bcvsolutions.idm.core.rest.lookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.rest.lookup.IdentifiableByNameLookup;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;

@Component
public class IdmConfigurationLookup extends IdentifiableByNameLookup<IdmConfiguration> {
	
	@Autowired 
	private ApplicationContext applicationContext;
	
	private IdmConfigurationService configurationService;
	
	/**
	 * We need to inject repository lazily - we need security AOP to take effect
	 *   
	 * @return
	 */
	@Override
	protected IdmConfigurationService getEntityService() {
		if (configurationService == null) { 
			configurationService = applicationContext.getBean(IdmConfigurationService.class);
		}
		return configurationService;
	}
}
