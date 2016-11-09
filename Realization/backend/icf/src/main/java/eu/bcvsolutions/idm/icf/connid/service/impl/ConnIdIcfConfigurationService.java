package eu.bcvsolutions.idm.icf.connid.service.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertyImpl;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.icf.IcfModuleDescriptor;
import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperties;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperty;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfEnabledAttribute;
import eu.bcvsolutions.idm.icf.api.IcfObjectPoolConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfPasswordAttribute;
import eu.bcvsolutions.idm.icf.api.IcfSchema;
import eu.bcvsolutions.idm.icf.connid.domain.ConnIdIcfConvertUtil;
import eu.bcvsolutions.idm.icf.dto.IcfConfigurationPropertiesDto;
import eu.bcvsolutions.idm.icf.dto.IcfConfigurationPropertyDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorConfigurationDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorInfoDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorKeyDto;
import eu.bcvsolutions.idm.icf.dto.IcfObjectPoolConfigurationDto;
import eu.bcvsolutions.idm.icf.exception.IcfException;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationService;
import eu.bcvsolutions.idm.icf.service.impl.DefaultIcfConfigurationFacade;

@Service
public class ConnIdIcfConfigurationService implements IcfConfigurationService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnIdIcfConfigurationService.class);

	// Cached local connid managers
	private List<ConnectorInfoManager> managers;

	@Autowired
	public ConnIdIcfConfigurationService(DefaultIcfConfigurationFacade icfConfigurationAggregator) {
		if (icfConfigurationAggregator.getIcfConfigs() == null) {
			throw new IcfException("Map of ICF implementations is not defined!");
		}
		if (icfConfigurationAggregator.getIcfConfigs().containsKey(this.getIcfType())) {
			throw new IcfException("ICF implementation duplicity for key: " + this.getIcfType());
		}
		icfConfigurationAggregator.getIcfConfigs().put(this.getIcfType(), this);
	}

	/**
	 * Return key defined ICF implementation
	 * 
	 * @return
	 */
	@Override
	public String getIcfType() {
		return "connId";
	}

	/**
	 * Return available local connectors for this ICF implementation
	 * 
	 * @return
	 */
	@Override
	public List<IcfConnectorInfo> getAvailableLocalConnectors() {
		log.info("Get Available local connectors - ConnId");
		List<IcfConnectorInfo> localConnectorInfos = new ArrayList<>();
		List<ConnectorInfoManager> managers = findAllLocalConnectorManagers();

		for (ConnectorInfoManager manager : managers) {
			List<ConnectorInfo> infos = manager.getConnectorInfos();
			if (infos == null) {
				continue;
			}
			for (ConnectorInfo info : infos) {
				ConnectorKey key = info.getConnectorKey();
				if (key == null) {
					continue;
				}
				IcfConnectorKeyDto keyDto = new IcfConnectorKeyDto(getIcfType(), key.getBundleName(),
						key.getBundleVersion(), key.getConnectorName());
				IcfConnectorInfoDto infoDto = new IcfConnectorInfoDto(info.getConnectorDisplayName(),
						info.getConnectorCategory(), keyDto);
				localConnectorInfos.add(infoDto);
			}
		}
		return localConnectorInfos;
	}

	/**
	 * Return find connector default configuration by connector info
	 * 
	 * @param info
	 * @return
	 */
	@Override
	public IcfConnectorConfiguration getConnectorConfiguration(IcfConnectorInfo info) {
		Assert.notNull(info);

		ConnectorInfo i = getConnIdConnectorInfo(info.getConnectorKey());
		if (i != null) {
			APIConfiguration apiConf = i.createDefaultAPIConfiguration();
			IcfConnectorConfiguration configDto = ConnIdIcfConvertUtil.convertConnIdConnectorConfiguration(apiConf);
			return configDto;
		}
		return null;
	}

	public ConnectorInfo getConnIdConnectorInfo(IcfConnectorKey key) {
		Assert.notNull(key);

		for (ConnectorInfoManager manager : findAllLocalConnectorManagers()) {
			ConnectorInfo i = manager
					.findConnectorInfo(ConnIdIcfConvertUtil.convertConnectorKeyFromDto(key, this.getIcfType()));
			if (i != null) {
				return i;
			}
		}
		return null;
	}

	@Override
	public IcfSchema getSchema(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		log.info("Get Schema - ConnId (" + key.toString() + ")");
		ConnectorFacade conn = getConnectorFacade(key, connectorConfiguration);
		Schema schema = conn.schema();
		IcfSchema icfSchema = ConnIdIcfConvertUtil.convertConnIdSchema(schema);
		return icfSchema;
	}

	private List<ConnectorInfoManager> findAllLocalConnectorManagers() {
		if (managers == null) {
			managers = new ArrayList<>();
			Reflections reflections = new Reflections();
			Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(ConnectorClass.class);

			for (Class<?> clazz : annotated) {
				URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
				ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();
				ConnectorInfoManager manager = fact.getLocalManager(url);
				managers.add(manager);
			}
		}
		return managers;
	}

	private ConnectorFacade getConnectorFacade(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		ConnectorInfo connIdInfo = this.getConnIdConnectorInfo(key);
		Assert.notNull(connIdInfo, "ConnId connector info not found!");
		APIConfiguration config = connIdInfo.createDefaultAPIConfiguration();
		Assert.notNull(config.getConfigurationProperties(), "ConnId connector configuration properties not found!");
		config = ConnIdIcfConvertUtil.convertIcfConnectorConfiguration(connectorConfiguration, config);
		// Use the ConnectorFacadeFactory's newInstance() method to get a new
		// connector.
		ConnectorFacade conn = ConnectorFacadeFactory.getManagedInstance().newInstance(config);

		// Make sure we have set up the Configuration properly
		conn.validate();
		return conn;
	}

}