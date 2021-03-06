package eu.bcvsolutions.idm.test.api;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@Ignore
public abstract class AbstractRestTest extends AbstractIntegrationTest {

	private MockMvc mockMvc;
	
	@Autowired
    private WebApplicationContext webApplicationContext;
	
	@Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
	}
	
	public MockMvc getMockMvc() {
		return mockMvc;
	}
}
