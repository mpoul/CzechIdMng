package eu.bcvsolutions.idm.icf.service.api;

import java.util.List;

import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfSchema;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorInfoDto;

public interface IcfConfigurationService {

	/**
	 * Return key defined ICF implementation
	 * @return
	 */
	String getIcfType();

	/**
	 * Return available local connectors for this ICF implementation
	 * @return
	 */
	List<IcfConnectorInfo> getAvailableLocalConnectors();

	/**
	 * Return find connector default configuration by connector info
	 * @param info
	 * @return
	 */
	IcfConnectorConfiguration getConnectorConfiguration(IcfConnectorInfo info);
	

	/**
	 * Return schema for connector and given configuration. Schema contains list of attribute definitions in object classes.
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @return
	 */
	IcfSchema getSchema(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration);

}