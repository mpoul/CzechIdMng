package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.SchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.dto.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysConnectorKey;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.FormPropertyManager;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSynchronizationConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityHandlingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperties;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import eu.bcvsolutions.idm.ic.api.IcSchema;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertiesImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorConfigurationImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorKeyImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;

/**
 * Deafult target system configuration service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysSystemService extends AbstractFormableService<SysSystem, QuickFilter>
		implements SysSystemService {

	private final SysSystemRepository systemRepository;
	private final IcConfigurationFacade icConfigurationFacade;
	private final SysSchemaObjectClassService objectClassService;
	private final SysSchemaAttributeService attributeService;
	private final SysRoleSystemService roleSystemService;
	private final SysSystemEntityRepository systemEntityRepository;
	private final AccAccountRepository accountRepository;
	private final SysSystemEntityHandlingService systemEntityHandlingService;
	private final SysSynchronizationConfigService synchronizationConfigService;
	private final FormPropertyManager formPropertyManager;

	@Autowired
	public DefaultSysSystemService(
			SysSystemRepository systemRepository,
			FormService formService,
			IcConfigurationFacade icConfigurationFacade, 
			SysSchemaObjectClassService objectClassService,
			SysSchemaAttributeService attributeService,
			SysRoleSystemService roleSystemService,
			SysSystemEntityRepository systemEntityRepository,
			AccAccountRepository accountRepository,
			SysSystemEntityHandlingService systemEntityHandlingService,
			SysSynchronizationConfigService synchronizationConfigService,
			FormPropertyManager formPropertyManager) {
		super(systemRepository, formService);
		//
		Assert.notNull(icConfigurationFacade);
		Assert.notNull(objectClassService);
		Assert.notNull(attributeService);
		Assert.notNull(roleSystemService);
		Assert.notNull(systemEntityRepository);
		Assert.notNull(accountRepository);
		Assert.notNull(systemEntityHandlingService);
		Assert.notNull(synchronizationConfigService);
		Assert.notNull(formPropertyManager);
		//
		this.systemRepository = systemRepository;
		this.icConfigurationFacade = icConfigurationFacade;
		this.objectClassService = objectClassService;
		this.attributeService = attributeService;
		this.roleSystemService = roleSystemService;
		this.systemEntityRepository = systemEntityRepository;
		this.accountRepository = accountRepository;
		this.systemEntityHandlingService = systemEntityHandlingService;
		this.synchronizationConfigService = synchronizationConfigService;
		this.formPropertyManager = formPropertyManager;
	}

	@Override
	@Transactional
	public void delete(SysSystem system) {
		Assert.notNull(system);
		//
		// if exists accounts or system entities, then system could not be deleted
		if (systemEntityRepository.countBySystem(system) > 0) {
			throw new ResultCodeException(AccResultCode.SYSTEM_DELETE_FAILED_HAS_ENTITIES, ImmutableMap.of("system", system.getName()));
		}
		if (accountRepository.countBySystem(system) > 0) {
			throw new ResultCodeException(AccResultCode.SYSTEM_DELETE_FAILED_HAS_ACCOUNTS, ImmutableMap.of("system", system.getName()));
		}
		// delete mapped roles
		RoleSystemFilter roleSystemFilter = new RoleSystemFilter();
		roleSystemFilter.setSystemId(system.getId());
		roleSystemService.find(roleSystemFilter, null).forEach(roleSystem -> {
			roleSystemService.delete(roleSystem);
		});
		// delete all mappings
		systemEntityHandlingService.findBySystem(system, null, null).forEach(systemEntityHandling -> {
			systemEntityHandlingService.delete(systemEntityHandling);
		});
		SchemaObjectClassFilter filter = new SchemaObjectClassFilter();
		filter.setSystemId(system.getId());	
		objectClassService.find(filter, null).forEach(schemaObjectClass -> {
			objectClassService.delete(schemaObjectClass);
		});
		// delete synchronization configs
		SynchronizationConfigFilter synchronizationConfigFilter = new SynchronizationConfigFilter();
		synchronizationConfigFilter.setSystemId(system.getId());
		synchronizationConfigService.find(synchronizationConfigFilter, null).forEach(config -> {
			synchronizationConfigService.delete(config);
		});
		//
		super.delete(system);
	}

	@Override
	@Transactional(readOnly = true)
	public SysSystem getByName(String name) {
		return systemRepository.findOneByName(name);
	}

	@Override
	@Transactional
	public IcConnectorConfiguration getConnectorConfiguration(SysSystem system) {
		Assert.notNull(system);
		
		if(system.getConnectorKey() == null){
			return null;
		}
		// load connector properties
		IcConnectorConfiguration connectorConfig = icConfigurationFacade.getConnectorConfiguration(system.getConnectorKey());
		// load filled form values
		IdmFormDefinition formDefinition = getConnectorFormDefinition(system.getConnectorKey());
		List<AbstractFormValue<SysSystem>> formValues = getFormService().getValues(system, formDefinition);
		Map<String, List<AbstractFormValue<SysSystem>>> attributeValues = getFormService().toValueMap(formValues);
		// fill connector configuration from form values
		IcConnectorConfigurationImpl icConf = new IcConnectorConfigurationImpl();
		IcConfigurationProperties properties = new IcConfigurationPropertiesImpl();
		icConf.setConfigurationProperties(properties);
		//
		for (short seq = 0; seq < connectorConfig.getConfigurationProperties().getProperties().size(); seq++) {
			IcConfigurationProperty propertyConfig = connectorConfig.getConfigurationProperties().getProperties().get(seq);
			IdmFormAttribute formAttribute = formDefinition.getMappedAttributeByName(propertyConfig.getName());
			List<AbstractFormValue<SysSystem>> eavAttributeValues = attributeValues.get(formAttribute.getName());
			// create property instance from configuration
			IcConfigurationProperty property = formPropertyManager.toConnectorProperty(propertyConfig, eavAttributeValues);
			if (property.getValue() != null) {
				// only filled values to configuration
				properties.getProperties().add(property);
			}
		}
		return icConf;
	}

	@Override
	@Transactional
	public void generateSchema(SysSystem system) {
		Assert.notNull(system);

		// Find connector identification persisted in system
		IcConnectorKey connectorKey = system.getConnectorKey();
		if (connectorKey == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persisted in system
		IcConnectorConfiguration connectorConfig = getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Call IC module and find schema for given connector key and
		// configuration
		IcSchema icSchema = icConfigurationFacade.getSchema(connectorKey, connectorConfig);
		if (icSchema == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_SCHEMA_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Load existing object class from system
		SchemaObjectClassFilter objectClassFilter = new SchemaObjectClassFilter();
		objectClassFilter.setSystemId(system.getId());
		List<SysSchemaObjectClass> sysObjectClassesInSystem = null;
		Page<SysSchemaObjectClass> page = objectClassService.find(objectClassFilter, null);
		sysObjectClassesInSystem = page.getContent();

		// Convert IC schema to ACC entities
		List<SysSchemaObjectClass> sysObjectClasses = new ArrayList<SysSchemaObjectClass>();
		List<SysSchemaAttribute> sysAttributes = new ArrayList<SysSchemaAttribute>();
		for (IcObjectClassInfo objectClass : icSchema.getDeclaredObjectClasses()) {
			
			// We can create only IC schemas, it means only schemas created for __ACCOUNT__ and __GROUP__
			if(!(objectClass.getType().startsWith("__") && objectClass.getType().endsWith("__"))){
				continue;
			}
			SysSchemaObjectClass sysObjectClass = null;
			// If existed some object class in system, then we will compared
			// every object with object class in resource
			// If will be same (same name), then we do only refresh object
			// values from resource
			if (sysObjectClassesInSystem != null) {
				Optional<SysSchemaObjectClass> objectClassSame = sysObjectClassesInSystem.stream()
						.filter(objectClassInSystem -> { //
							return objectClassInSystem.getObjectClassName().equals(objectClass.getType());
						}) //
						.findFirst();
				if (objectClassSame.isPresent()) {
					sysObjectClass = objectClassSame.get();
				}
			}
			// Convert IC object class to ACC (if is null, then will be created
			// new instance)
			sysObjectClass = convertIcObjectClassInfo(objectClass, sysObjectClass);
			sysObjectClass.setSystem(system);
			sysObjectClasses.add(sysObjectClass);

			List<SysSchemaAttribute> attributesInSystem = null;
			// Load existing attributes for existing object class in system
			if (sysObjectClass.getId() != null) {
				SchemaAttributeFilter attFilter = new SchemaAttributeFilter();
				attFilter.setSystemId(system.getId());
				attFilter.setObjectClassId(sysObjectClass.getId());

				Page<SysSchemaAttribute> attributesInSystemPage = attributeService.find(attFilter, null);
				attributesInSystem = attributesInSystemPage.getContent();
			}
			for (IcAttributeInfo attribute : objectClass.getAttributeInfos()) {
				// If will be IC and ACC attribute same (same name), then we
				// will do only refresh object values from resource
				SysSchemaAttribute sysAttribute = null;
				if (attributesInSystem != null) {
					Optional<SysSchemaAttribute> sysAttributeOptional = attributesInSystem.stream().filter(a -> {
						return a.getName().equals(attribute.getName());
					}).findFirst();
					if (sysAttributeOptional.isPresent()) {
						sysAttribute = sysAttributeOptional.get();
					}
				}
				sysAttribute = convertIcAttributeInfo(attribute, sysAttribute);
				sysAttribute.setObjectClass(sysObjectClass);
				sysAttributes.add(sysAttribute);
			}
		}

		// Persist generated schema to system
		objectClassService.saveAll(sysObjectClasses);
		attributeService.saveAll(sysAttributes);
	}

	private SysSchemaObjectClass convertIcObjectClassInfo(IcObjectClassInfo objectClass,
			SysSchemaObjectClass sysObjectClass) {
		if (objectClass == null) {
			return null;
		}
		if (sysObjectClass == null) {
			sysObjectClass = new SysSchemaObjectClass();
		}
		sysObjectClass.setObjectClassName(objectClass.getType());
		sysObjectClass.setAuxiliary(objectClass.isAuxiliary());
		sysObjectClass.setContainer(objectClass.isContainer());
		return sysObjectClass;
	}

	private SysSchemaAttribute convertIcAttributeInfo(IcAttributeInfo attributeInfo,
			SysSchemaAttribute sysAttribute) {
		if (attributeInfo == null) {
			return null;
		}
		if (sysAttribute == null) {
			sysAttribute = new SysSchemaAttribute();
		}
		sysAttribute.setClassType(attributeInfo.getClassType());
		sysAttribute.setName(attributeInfo.getName());
		sysAttribute.setMultivalued(attributeInfo.isMultivalued());
		sysAttribute.setNativeName(attributeInfo.getNativeName());
		sysAttribute.setReadable(attributeInfo.isReadable());
		sysAttribute.setRequired(attributeInfo.isRequired());
		sysAttribute.setReturnedByDefault(attributeInfo.isReturnedByDefault());
		sysAttribute.setUpdateable(attributeInfo.isUpdateable());
		sysAttribute.setCreateable(attributeInfo.isCreateable());
		return sysAttribute;
	}

	@Override
	@Transactional
	public IdmFormDefinition getConnectorFormDefinition(IcConnectorKey connectorKey) {
		Assert.notNull(connectorKey);
		//
		// if form definition for given key already exists
		IdmFormDefinition formDefinition = getFormService().getDefinition(connectorKey.getConnectorName(),
				connectorKey.getFullName());
		if (formDefinition == null) {
			// we creates new form definition
			formDefinition = createConnectorFormDefinition(connectorKey);
		}
		return formDefinition;
	}

	/**
	 * Create form definition to given connectorKey by connector properties
	 * 
	 * @param connectorKey
	 * @return
	 */
	private synchronized IdmFormDefinition createConnectorFormDefinition(IcConnectorKey connectorKey) {
		IcConnectorConfiguration conf = icConfigurationFacade.getConnectorConfiguration(connectorKey);
		if (conf == null) {
			throw new IllegalStateException(MessageFormat.format("Connector with key [{0}] was not found on classpath.",
					connectorKey.getFullName()));
		}
		//
		List<IdmFormAttribute> formAttributes = new ArrayList<>();
		for (short seq = 0; seq < conf.getConfigurationProperties().getProperties().size(); seq++) {
			IcConfigurationProperty property = conf.getConfigurationProperties().getProperties().get(seq);
			IdmFormAttribute attribute = formPropertyManager.toFormAttribute(property);
			attribute.setSeq(seq);
			formAttributes.add(attribute);
		}
		return getFormService().createDefinition(connectorKey.getConnectorName(), connectorKey.getFullName(),
				formAttributes);
	}

	@Deprecated
	@Transactional
	public SysSystem createTestSystem() {
		// create owner
		SysSystem system = new SysSystem();
		system.setName("sysOne_" + System.currentTimeMillis());
		system.setConnectorKey(new SysConnectorKey(getTestConnectorKey()));
		save(system);

		IdmFormDefinition savedFormDefinition = getConnectorFormDefinition(system.getConnectorKey());

		List<SysSystemFormValue> values = new ArrayList<>();
		SysSystemFormValue host = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("host"));
		host.setValue("localhost");
		values.add(host);
		SysSystemFormValue port = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("port"));
		port.setValue("5432");
		values.add(port);
		SysSystemFormValue user = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("user"));
		user.setValue("idmadmin");
		values.add(user);
		SysSystemFormValue password = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("password"));
		password.setValue("idmadmin");
		values.add(password);
		SysSystemFormValue database = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("database"));
		database.setValue("bcv_idm_storage");
		values.add(database);
		SysSystemFormValue table = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("table"));
		table.setValue("system_users");
		values.add(table);
		SysSystemFormValue keyColumn = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("keyColumn"));
		keyColumn.setValue("name");
		values.add(keyColumn);
		SysSystemFormValue passwordColumn = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("passwordColumn"));
		passwordColumn.setValue("password");
		values.add(passwordColumn);
		SysSystemFormValue allNative = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("allNative"));
		allNative.setValue(Boolean.TRUE);
		values.add(allNative);
		SysSystemFormValue jdbcDriver = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("jdbcDriver"));
		jdbcDriver.setValue("org.postgresql.Driver");
		values.add(jdbcDriver);
		SysSystemFormValue jdbcUrlTemplate = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("jdbcUrlTemplate"));
		jdbcUrlTemplate.setValue("jdbc:postgresql://%h:%p/%d");
		values.add(jdbcUrlTemplate);
		SysSystemFormValue rethrowAllSQLExceptions = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("rethrowAllSQLExceptions"));
		rethrowAllSQLExceptions.setValue(Boolean.TRUE);
		values.add(rethrowAllSQLExceptions);
		SysSystemFormValue statusColumn = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("statusColumn"));
		statusColumn.setValue("status");
		values.add(statusColumn);
		SysSystemFormValue disabledStatusValue = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("disabledStatusValue"));
		disabledStatusValue.setValue("disabled");
		values.add(disabledStatusValue);
		SysSystemFormValue enabledStatusValue = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("enabledStatusValue"));
		enabledStatusValue.setValue("enabled");
		values.add(enabledStatusValue);

		getFormService().saveValues(system, savedFormDefinition, values);

		return system;
	}

	/**
	 * Basic table connector
	 * 
	 * @return
	 */
	@Deprecated
	public IcConnectorKey getTestConnectorKey() {
		IcConnectorKeyImpl key = new IcConnectorKeyImpl();
		key.setFramework("connId");
		key.setConnectorName("net.tirasa.connid.bundles.db.table.DatabaseTableConnector");
		key.setBundleName("net.tirasa.connid.bundles.db.table");
		key.setBundleVersion("2.2.4");
		return key;
	}
}
