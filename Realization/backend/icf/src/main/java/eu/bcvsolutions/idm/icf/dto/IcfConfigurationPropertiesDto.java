package eu.bcvsolutions.idm.icf.dto;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperties;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperty;

public class IcfConfigurationPropertiesDto implements IcfConfigurationProperties {

    
    List<IcfConfigurationProperty> properties;

    /**
     * The list of properties {@link IcfConfigurationProperty}.
     */
	@Override
	public List<IcfConfigurationProperty> getProperties() {
		if(properties == null){
			properties = new ArrayList<>();
		}
		return properties;
	}

	public void setProperties(List<IcfConfigurationProperty> properties) {
		this.properties = properties;
	}
}