package eu.bcvsolutions.idm.ic.service.api;

import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcSchema;

/**
 * Facade for get available connectors configuration
 * @author svandav
 *
 */
public interface IcConfigurationFacade {

	/**
	 * Return available local connectors for all IC implementations
	 *
	 */
	Map<String, List<IcConnectorInfo>> getAvailableLocalConnectors();

	/**
	 * Return all registered IC configuration service implementations
	 * @return
	 */
	Map<String, IcConfigurationService> getIcConfigs();
	
	/**
	 * Return find connector default configuration by connector key
	 * 
	 * @param key
	 * @return
	 */
	
	IcConnectorConfiguration getConnectorConfiguration(IcConnectorKey key);

	/**
	 * Return schema for connector and given configuration. Schema contains list of attribute definitions in object classes.
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @return
	 */
	IcSchema getSchema(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration);

}