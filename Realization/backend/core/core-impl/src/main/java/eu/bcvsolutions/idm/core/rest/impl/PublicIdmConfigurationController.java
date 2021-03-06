package eu.bcvsolutions.idm.core.rest.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.dto.ConfigurationDto;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;

/**
 * Provides public configurations
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/public/configurations")
public class PublicIdmConfigurationController implements BaseEntityController<IdmConfiguration> {
	
	private final IdmConfigurationService configurationService;
	
	@Autowired
	public PublicIdmConfigurationController(IdmConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	/**
	 * Returns all public configuration properties 
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public List<ConfigurationDto> getAllPublicConfigurations() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllPublicConfigurations();
	}
	
	/**
	 * Bulk configuration save
	 * 
	 * TODO: move to better controller - IdmConfigurationController could not be used (consumes does not work in repository rest controller)
	 * 
	 * @param configuration
	 * @return
	 * @throws IOException 
	 */
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@RequestMapping(value = "/bulk/save", method = RequestMethod.PUT, consumes = MediaType.TEXT_PLAIN_VALUE)
	public void saveProperties(@RequestBody String configuration) throws IOException {
		Properties p = new Properties();
	    p.load(new StringReader(configuration));
	    p.forEach((name, value) -> {
	    	configurationService.saveValue(name.toString(), value == null ? null : value.toString().split("#")[0].trim());
	    });
	}

}
