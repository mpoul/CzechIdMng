package eu.bcvsolutions.idm.workflow.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import eu.bcvsolutions.idm.workflow.api.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.workflow.api.service.WorkflowDeploymentService;

/**
 * Test history of process and tasks
 * 
 * @author svandav
 *
 */
public class HistoryProcessAndTaskTest extends AbstractWorkflowIntegrationTest {

	private static final String PROCESS_KEY = "testHistoryProcessAndTask";

	@Autowired
	private WorkflowHistoricProcessInstanceService historicProcessService;
	@Autowired
	private WorkflowProcessInstanceService processInstanceService;
	@Autowired
	private WorkflowDeploymentService processDeploymentService;
	@Autowired
	private WorkflowTaskInstanceService taskInstanceService;
	@Autowired
	private WorkflowHistoricTaskInstanceService historicTaskService;

	@Before
	public void login() {
		super.loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void deployAndRunProcess() {
		//Deploy process
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream("eu/bcvsolutions/idm/core/workflow/history/testHistoryProcessAndTask.bpmn20.xml");
		WorkflowDeploymentDto deploymentDto = processDeploymentService.create(PROCESS_KEY, "test.bpmn20.xml", is);
		assertNotNull(deploymentDto);

		//Start instance of process
		ProcessInstance instance = processInstanceService.startProcess(PROCESS_KEY, null, InitTestData.TEST_USER_1, null,
				null);
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(instance.getId());;
		ResourcesWrapper<WorkflowProcessInstanceDto> processes = processInstanceService.search(filter);
		
		assertEquals(PROCESS_KEY, ((List<WorkflowProcessInstanceDto>) processes.getResources()).get(0).getName());
		WorkflowHistoricProcessInstanceDto historicProcessDto = historicProcessService.get(instance.getId());
		assertNotNull(historicProcessDto);

		this.logout();
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		// Applicant for this process is testUser1. For testUser2 must be result
		// null
		WorkflowHistoricProcessInstanceDto historicProcessDto2 = historicProcessService.get(instance.getId());
		assertNull(historicProcessDto2);

		this.logout();
		this.loginAsAdmin(InitTestData.TEST_USER_1);
		
		completeTasksAndCheckHistory();
	}

	
	private void completeTasksAndCheckHistory() {

		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessDefinitionKey(PROCESS_KEY);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) taskInstanceService.search(filter).getResources();
		assertEquals(1, tasks.size());
		assertEquals("userTaskFirst", tasks.get(0).getName());
		String taskId = tasks.get(0).getId();
		String processId = tasks.get(0).getProcessInstanceId();
		
		taskInstanceService.completeTask(taskId, null);
		
		//Check task history
		checkTaskHistory(taskId, InitTestData.TEST_USER_1);
		
		//Second task is for testUser2 (is candidate) for testUser1 must be null
		tasks = (List<WorkflowTaskInstanceDto>) taskInstanceService.search(filter).getResources();
		assertEquals(0, tasks.size());

		this.logout();
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		tasks = (List<WorkflowTaskInstanceDto>) taskInstanceService.search(filter).getResources();
		assertEquals(1, tasks.size());
		assertEquals("userTaskSecond", tasks.get(0).getName());
		taskId = tasks.get(0).getId();
		taskInstanceService.completeTask(taskId, null);
		
		//Check task history
		checkTaskHistory(taskId, InitTestData.TEST_USER_2);

		tasks = (List<WorkflowTaskInstanceDto>) taskInstanceService.search(filter).getResources();
		assertEquals(0, tasks.size());
		
		//Find history of process. Historic process must exist and must be ended.
		WorkflowHistoricProcessInstanceDto historicProcess = historicProcessService.get(processId);
		assertNotNull(historicProcess);
		assertNotNull(historicProcess.getEndTime());

	}

	/**
	 * Check task history
	 * @param taskId
	 */
	private void checkTaskHistory(String taskId, String assignee) {
		WorkflowHistoricTaskInstanceDto taskHistory = historicTaskService.get(taskId);
		assertNotNull(taskHistory);
		assertEquals("completed", taskHistory.getDeleteReason());
		assertEquals(assignee, taskHistory.getAssignee());
		assertEquals(taskId, taskHistory.getId());
	}
}
