package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.Plugin;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.exception.ModuleNotDisableableException;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.security.api.domain.GroupPermission;

/**
 * Default implementation for {@link ModuleDescriptor} administrative.
 * 
 * @author Radek Tomiška
 *
 * @see ModuleDescriptor
 * @see Plugin
 * @see PluginRegistry
 */
@Service
public class DefaultModuleService implements ModuleService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultModuleService.class);
	
	private final PluginRegistry<ModuleDescriptor, String> moduleDescriptorRegistry;
	private final IdmConfigurationService configurationService;

	@Autowired
	public DefaultModuleService(PluginRegistry<ModuleDescriptor, String> moduleDescriptorRegistry,
			IdmConfigurationService configurationService) {
		Assert.notNull(moduleDescriptorRegistry, "Module registry is required!");
		Assert.notNull(configurationService, "ConfigurationService is required!");

		this.moduleDescriptorRegistry = moduleDescriptorRegistry;
		this.configurationService = configurationService;
	}

	@Override
	public List<ModuleDescriptor> getInstalledModules() {
		List<ModuleDescriptor> registeredModules = new ArrayList<>();
		moduleDescriptorRegistry.forEach(moduleDescriptor -> {
			registeredModules.add(moduleDescriptor);
		});
		return Collections.unmodifiableList(registeredModules);
	}
	
	public ModuleDescriptor getModule(String moduleId) {
		return moduleDescriptorRegistry.getPluginFor(moduleId);
	}

	@Override
	public List<ModuleDescriptor> getEnabledModules() {
		return Collections.unmodifiableList( //
				getInstalledModules() //
				.stream() //
				.filter(moduleDescriptor -> { //
					return isEnabled(moduleDescriptor);
				}) //
				.collect(Collectors.toList()));
	}

	@Override
	public boolean isEnabled(String moduleId) {
		return isEnabled(moduleDescriptorRegistry.getPluginFor(moduleId));
	}
	
	@Override
	public boolean isEnabled(ModuleDescriptor moduleDescriptor) {
		// if module not exists, then is disabled by default
		if (moduleDescriptor == null) {
			return false;
		}
		if (!moduleDescriptor.isDisableable()) {
			return true;
		}
		return configurationService.getBooleanValue(
				getModuleConfigurationProperty(moduleDescriptor.getId(), PROPERTY_ENABLED), false);
	}

	@Override
	public void enable(String moduleId) {
		setEnabled(moduleId, true);
	}

	@Override
	public void disable(String moduleId) {
		setEnabled(moduleId, false);
	}

	@Override
	public void setEnabled(String moduleId, boolean enabled) {
		ModuleDescriptor moduleDescriptor = moduleDescriptorRegistry.getPluginFor(moduleId);
		if (moduleDescriptor == null) {
			LOG.info("Frontend module [{}] will be enabled [{}].", moduleId, enabled);
		} else {
			if (!enabled && !moduleDescriptor.isDisableable()) {
				throw new ModuleNotDisableableException(moduleId);
			}
			LOG.info("Backend module [{}] will be enabled [{}].", moduleId, enabled);
			//
			// TODO: license check if module is enabling
		}		
		configurationService.setBooleanValue(
				getModuleConfigurationProperty(moduleId, PROPERTY_ENABLED), enabled);
	}
	
	@Override
	public List<GroupPermission> getAvailablePermissions() {
		List<GroupPermission> perrmissions = new ArrayList<>();
		getEnabledModules().forEach(moduleDescriptor -> {
			perrmissions.addAll(moduleDescriptor.getPermissions());
		});
		return Collections.unmodifiableList(perrmissions);
	}

	/**
	 * Returns module property by {@link IdmConfiguratioService} conventions.
	 * 
	 * @param moduleId
	 * @param property
	 * @return
	 */
	@Override
	public String getModuleConfigurationProperty(String moduleId, String property) {
		return IdmConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX //
				+ moduleId + IdmConfigurationService.PROPERTY_SEPARATOR + property;
	}

}
