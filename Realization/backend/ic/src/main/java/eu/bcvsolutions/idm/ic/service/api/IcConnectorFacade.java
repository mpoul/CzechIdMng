package eu.bcvsolutions.idm.ic.service.api;

import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

public interface IcConnectorFacade {
	
	public static final String PASSWORD_ATTRIBUTE_NAME = "__PASSWORD__";

	/**
	 * Create new object in resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param attributes - Attributes for new object
	 * @return
	 */
	IcUidAttribute createObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, List<IcAttribute> attributes);
	
	/**
	 * Replace attributes in exist object in resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param uid - Identification of object in resource
	 * @param replaceAttributes - Attributes to replace in resource object
	 * @return
	 */
	IcUidAttribute updateObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid, List<IcAttribute> replaceAttributes);
	
	/**
	 * Delete object with same uid from resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param uid - Identification of object in resource
	 */
	void deleteObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid);
	
	/**
	 * Read object with same uid from resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param uid - Identification of object in resource
	 * @return
	 */
	IcConnectorObject readObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid);
	
	/**
	 * Authenticate user
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param username
	 * @param password
	 * @return
	 */
	IcUidAttribute authenticateObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, String username, GuardedString password);
	
	/**
	 * @return Connector services for all ICs
	 */
	Map<String, IcConnectorService> getIcConnectors();

}