package eu.bcvsolutions.idm.icf.service.api;

import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfConnectorObject;
import eu.bcvsolutions.idm.icf.api.IcfObjectClass;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

public interface IcfConnectorFacade {
	
	public static final String PASSWORD_ATTRIBUTE_NAME = "__PASSWORD__";

	/**
	 * Create new object in resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param attributes - Attributes for new object
	 * @return
	 */
	IcfUidAttribute createObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, List<IcfAttribute> attributes);
	
	/**
	 * Replace attributes in exist object in resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param uid - Identification of object in resource
	 * @param replaceAttributes - Attributes to replace in resource object
	 * @return
	 */
	IcfUidAttribute updateObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, IcfUidAttribute uid, List<IcfAttribute> replaceAttributes);
	
	/**
	 * Delete object with same uid from resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param uid - Identification of object in resource
	 */
	void deleteObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, IcfUidAttribute uid);
	
	/**
	 * Read object with same uid from resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param uid - Identification of object in resource
	 * @return
	 */
	IcfConnectorObject readObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, IcfUidAttribute uid);
	
	/**
	 * Authenticate user
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param username
	 * @param password
	 * @return
	 */
	IcfUidAttribute authenticateObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			IcfObjectClass objectClass, String username, GuardedString password);
	
	/**
	 * @return Connector services for all ICFs
	 */
	Map<String, IcfConnectorService> getIcfConnectors();

}