package eu.bcvsolutions.idm.acc.event;

/**
 * Defines provisioning priority (+after, -before)
 * 
 * @author Radek Tomiška
 *
 */
public interface ProvisioningEvent {

	static final int DEFAULT_PROVISIONING_ORDER = 1000;
}
