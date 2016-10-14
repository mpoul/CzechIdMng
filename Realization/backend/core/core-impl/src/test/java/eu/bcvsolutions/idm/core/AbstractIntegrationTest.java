package eu.bcvsolutions.idm.core;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import eu.bcvsolutions.idm.IdmApplication;
import eu.bcvsolutions.idm.security.api.service.SecurityService;
import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;

/**
 * Test rest services will be based on spring integration tests with MockMvc / hamcrest and junit test framework
 * 
 * http://docs.spring.io/spring-framework/docs/current/spring-framework-reference/html/integration-testing.html
 * 
 * @author Radek Tomiška 
 *
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IdmApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
@ActiveProfiles("test")
@Rollback(true)
public abstract class AbstractIntegrationTest {
	
	@Autowired
	private SecurityService securityService;
	
	public void loginAsAdmin(String username) {
		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication(username, null, securityService.getAllAvailableAuthorities()));
	}
	
	public void logout(){
		SecurityContextHolder.clearContext();
	}
}