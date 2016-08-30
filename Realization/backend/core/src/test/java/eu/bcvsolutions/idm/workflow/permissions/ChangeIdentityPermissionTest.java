package eu.bcvsolutions.idm.workflow.permissions;

import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractWorkflowTest;
import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.rest.IdmIdentityController;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.rest.WorkflowTaskInstanceController;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;

/**
 * Test change permissions for identity
 * 
 * @author svandav
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChangeIdentityPermissionTest extends AbstractWorkflowTest {

	private static final String ADDED_IDENTITY_ROLES_VARIABLE = "addedIdentityRoles";
	private static final String REMOVED_IDENTITY_ROLES_VARIABLE = "removedIdentityRoles";
	private static final String CHANGED_IDENTITY_ROLES_VARIABLE = "changedIdentityRoles";

	@Autowired
	private WorkflowTaskInstanceService taskInstanceService;
	@Autowired
	private IdmIdentityController idmIdentityController;
	@Autowired
	private IdmIdentityRepository idmIdentityRepository;
	@Autowired
	private IdmRoleRepository idmRoleRepository;
	@Autowired
	private IdmIdentityRoleRepository idmIdentityRoleRepository;
	@Autowired
	private WorkflowTaskInstanceController workflowTaskInstanceController;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Before
	public void login() {
		super.loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void addApprovableSuperAdminRole() {
		IdmIdentity test1;
		Page<IdmIdentityRole> idmIdentityRolePage;
		WorkflowTaskInstanceDto createChangeRequest = startChangePermissions(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE, false);
		List<Map<String,Object>> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		roles.add(createNewPermission(InitTestData.TEST_ADMIN_ROLE, null, null));
		variables.put(ADDED_IDENTITY_ROLES_VARIABLE, roles);
		variables.put(CHANGED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(REMOVED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().isEmpty());
		
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.notEmpty(wrappedUserTasksResult.getBody().getResources());
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Deploy process for subprocess
		WorkflowDeploymentDto deploymentDtoSuperAdmin = deployProcess("eu/bcvsolutions/idm/core/workflow/role/approve/approveRoleBySuperAdminRole.bpmn20.xml");
		assertNotNull(deploymentDtoSuperAdmin);
		//Start subprocesses
		taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found no any task (because user2 approve his task)
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);

		this.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For admin must be found one task (because was started subprocess WF for approve add permission for all users with SuperAdminRole)
		Assert.notEmpty(wrappedUserTasksResult.getBody().getResources());
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByAdmin = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Approve add permission by admin
		taskInstanceService.completeTask(approveByAdmin.getId(), "approve", null, null);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For admin must be found no any task
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);
		
		test1 = idmIdentityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		idmIdentityRolePage = idmIdentityRoleRepository.findByIdentity(test1, null);
		final List<IdmIdentityRole> idmIdentityRoleList2 = new ArrayList<>();
		idmIdentityRolePage.forEach(s -> idmIdentityRoleList2.add(s));
		//User test 1 must have superAdminRole
		Assert.isTrue(idmIdentityRoleList2.stream().filter(s -> {return s.getRole().getName().equals(InitTestData.TEST_ADMIN_ROLE);}).findFirst().isPresent());
		
	}
	
	@Test
	public void changeApprovableSuperAdminRole() {
	
		WorkflowTaskInstanceDto createChangeRequest = startChangePermissions(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE, true);
		List<Map<String,Object>> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.MONTH, -1);
		Date validFrom = c.getTime();
		c.add(Calendar.MONTH, 2);
		Date validTill = c.getTime();
		IdmIdentityRole superAdminPermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE);
		
		//Validity date form and till must be null 
		Assert.isNull(superAdminPermission.getValidFrom());
		Assert.isNull(superAdminPermission.getValidTill());
		
		roles.add(createChangePermission(superAdminPermission.getId(), validFrom, validTill));
		variables.put(ADDED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(CHANGED_IDENTITY_ROLES_VARIABLE, roles);
		variables.put(REMOVED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().isEmpty());
		
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.notEmpty(wrappedUserTasksResult.getBody().getResources());
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Deploy process for subprocess
		WorkflowDeploymentDto deploymentDtoSuperAdmin = deployProcess("eu/bcvsolutions/idm/core/workflow/role/approve/approveRoleBySuperAdminRole.bpmn20.xml");
		assertNotNull(deploymentDtoSuperAdmin);
		//Start subprocesses
		taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found no any task (because user2 approve his task)
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);

		this.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For admin must be found one task (because was started subprocess WF for approve add permission for all users with SuperAdminRole)
		Assert.notEmpty(wrappedUserTasksResult.getBody().getResources());
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByAdmin = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Approve add permission by admin
		taskInstanceService.completeTask(approveByAdmin.getId(), "approve", null, null);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For admin must be found no any task
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);
		
		superAdminPermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE);
		//Validity date form and till must be not null 
		assertNotNull(superAdminPermission.getValidFrom());
		assertNotNull(superAdminPermission.getValidTill());
		// Validity date must be same as required validity on start of this test 
		Assert.isTrue(sdf.format(superAdminPermission.getValidFrom()).equals(sdf.format(validFrom)));
		Assert.isTrue(sdf.format(superAdminPermission.getValidTill()).equals(sdf.format(validTill)));
		
	}
	
	@Test
	public void changeNotApprovableUserRole() {
	
		WorkflowTaskInstanceDto createChangeRequest = startChangePermissions(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE, true);
		List<Map<String,Object>> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.MONTH, -1);
		Date validFrom = c.getTime();
		c.add(Calendar.MONTH, 2);
		Date validTill = c.getTime();
		IdmIdentityRole userRolePermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE);
		
		//Validity date form and till must be null 
		Assert.isNull(userRolePermission.getValidFrom());
		Assert.isNull(userRolePermission.getValidTill());
		
		roles.add(createChangePermission(userRolePermission.getId(), validFrom, validTill));
		variables.put(ADDED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(CHANGED_IDENTITY_ROLES_VARIABLE, roles);
		variables.put(REMOVED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().isEmpty());
		
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.notEmpty(wrappedUserTasksResult.getBody().getResources());
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Deploy process for subprocess
		WorkflowDeploymentDto deploymentDtoSuperAdmin = deployProcess("eu/bcvsolutions/idm/core/workflow/role/notapprove/notApproveRoleRealizationUpdate.bpmn20.xml");
		assertNotNull(deploymentDtoSuperAdmin);
		//Start subprocesses
		taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found no any task (because user2 approved task)
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);
		
		userRolePermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE);
		//Validity date form and till must be not null 
		assertNotNull(userRolePermission.getValidFrom());
		assertNotNull(userRolePermission.getValidTill());
		// Validity date must be same as required validity on start of this test 
		Assert.isTrue(sdf.format(userRolePermission.getValidFrom()).equals(sdf.format(validFrom)));
		Assert.isTrue(sdf.format(userRolePermission.getValidTill()).equals(sdf.format(validTill)));
		
	}
	
	@Test
	public void addNotApprovableUserRole() {
		IdmIdentity test1;
		Page<IdmIdentityRole> idmIdentityRolePage;
		WorkflowTaskInstanceDto createChangeRequest = startChangePermissions(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE, false);
		assertNotNull(createChangeRequest);
		List<Map<String,Object>> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		roles.add(createNewPermission(InitTestData.TEST_USER_ROLE, null, null));
		variables.put(ADDED_IDENTITY_ROLES_VARIABLE, roles);
		variables.put(CHANGED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(REMOVED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().isEmpty());
		
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.notEmpty(wrappedUserTasksResult.getBody().getResources());
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Deploy process for subprocess (without approving)
		WorkflowDeploymentDto deploymentDtoNotApprove = deployProcess("eu/bcvsolutions/idm/core/workflow/role/notapprove/notApproveRoleRealizationAdd.bpmn20.xml");
		assertNotNull(deploymentDtoNotApprove);
		
		//Start subprocesses
		taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found no any task (because user2 approve his task)
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);

		this.loginAsAdmin(InitTestData.TEST_USER_1);
		
		//UserRole is no approvable, therefore user test 1 must have userRole without any additional approving
		test1 = idmIdentityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		idmIdentityRolePage = idmIdentityRoleRepository.findByIdentity(test1, null);
		final List<IdmIdentityRole> idmIdentityRoleList2 = new ArrayList<>();
		idmIdentityRolePage.forEach(s -> idmIdentityRoleList2.add(s));
		//User test 1 must have superAdminRole
		Assert.isTrue(idmIdentityRoleList2.stream().filter(s -> {return s.getRole().getName().equals(InitTestData.TEST_USER_ROLE);}).findFirst().isPresent());
		
	}
	
	@Test
	public void removeApprovableSuperAdminRole() {
	
		WorkflowTaskInstanceDto createChangeRequest = startChangePermissions(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE, true);
		List<Long> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		IdmIdentityRole superAdminPermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE);
		// Add permission ID to remove list
		roles.add(superAdminPermission.getId());
		variables.put(ADDED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(CHANGED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(REMOVED_IDENTITY_ROLES_VARIABLE, roles);
		
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().isEmpty());
		
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.notEmpty(wrappedUserTasksResult.getBody().getResources());
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Deploy process for subprocess
		WorkflowDeploymentDto deploymentDtoSuperAdmin = deployProcess("eu/bcvsolutions/idm/core/workflow/role/approve/approveRemoveRoleBySuperAdminRole.bpmn20.xml");
		assertNotNull(deploymentDtoSuperAdmin);
		//Start subprocesses
		taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found no any task (because user2 approve his task)
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);

		this.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For admin must be found one task (because was started subprocess WF for approve add permission for all users with SuperAdminRole)
		Assert.notEmpty(wrappedUserTasksResult.getBody().getResources());
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByAdmin = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Approve add permission by admin
		taskInstanceService.completeTask(approveByAdmin.getId(), "approve", null, null);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For admin must be found no any task
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);
		
		superAdminPermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_ADMIN_ROLE);
		Assert.isNull(superAdminPermission);
		
	}
	
	@Test
	public void removeNotApprovableUserRole() {
	
		WorkflowTaskInstanceDto createChangeRequest = startChangePermissions(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE, true);
		List<Long> roles = new ArrayList<>();
		Map<String, Object> variables = new HashMap<>();
		IdmIdentityRole userRolePermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE);
		// Add permission ID to remove list
		roles.add(userRolePermission.getId());
		variables.put(ADDED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(CHANGED_IDENTITY_ROLES_VARIABLE, Lists.newArrayList());
		variables.put(REMOVED_IDENTITY_ROLES_VARIABLE, roles);
		
		taskInstanceService.completeTask(createChangeRequest.getId(), "createRequest", null, variables);
		ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user1 must found no tasks
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().isEmpty());
		
		this.loginAsAdmin(InitTestData.TEST_USER_2);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found one task (because user2 is manager for user1)
		Assert.notEmpty(wrappedUserTasksResult.getBody().getResources());
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 1);
		WorkflowTaskInstanceDto approveByManager = ((List<ResourceWrapper<WorkflowTaskInstanceDto>>)wrappedUserTasksResult.getBody().getResources()).get(0).getResource();
		
		//Deploy process for subprocess
		WorkflowDeploymentDto deploymentDtoSuperAdmin = deployProcess("eu/bcvsolutions/idm/core/workflow/role/notapprove/notApproveRoleRealizationRemove.bpmn20.xml");
		assertNotNull(deploymentDtoSuperAdmin);
		//Start subprocesses
		taskInstanceService.completeTask(approveByManager.getId(), "approve", null, variables);
		wrappedUserTasksResult =  workflowTaskInstanceController.getAll();
		// For user2 must be found no any task (because user2 approve his task)
		Assert.isTrue(wrappedUserTasksResult.getBody().getResources().size() == 0);

		userRolePermission = getPermission(InitTestData.TEST_USER_1, InitTestData.TEST_USER_ROLE);
		Assert.isNull(userRolePermission);
		
	}
	
	
	private IdmIdentityRole getPermission(String user, String roleName) {
		Page<IdmIdentityRole> idmIdentityRolePage = idmIdentityRoleRepository.findByIdentityUsername(user, null);
		final List<IdmIdentityRole> idmIdentityRoleList = new ArrayList<>();
		idmIdentityRolePage.forEach(s -> idmIdentityRoleList.add(s));
		IdmIdentityRole superAdminPermission = null;
		try{
			superAdminPermission = idmIdentityRoleList.stream().filter(s -> {return s.getRole().getName().equals(roleName);}).findFirst().get();
		}catch(NoSuchElementException ex){
			return null;
		}
		return superAdminPermission;
	}

	private WorkflowTaskInstanceDto startChangePermissions(String user, String role, boolean mustHaveRole) {
		//Deploy process
		WorkflowDeploymentDto deploymentDto = deployProcess("eu/bcvsolutions/idm/core/workflow/role/changeIdentityRoles.bpmn20.xml");
		assertNotNull(deploymentDto);
		
		//start change role process for TEST_USER_1
		this.loginAsAdmin(user);
		IdmIdentity test1 = idmIdentityRepository.findOneByUsername(user);
		
		Page<IdmIdentityRole> idmIdentityRolePage = idmIdentityRoleRepository.findByIdentity(test1, null);
		final List<IdmIdentityRole> idmIdentityRoleList = new ArrayList<>();
		idmIdentityRolePage.forEach(s -> idmIdentityRoleList.add(s));
		//User test 1 don't have superAdminRole yet
		boolean rolePresent = idmIdentityRoleList.stream().filter(s -> {return s.getRole().getName().equals(role);}).findFirst().isPresent();
		Assert.isTrue(mustHaveRole ? rolePresent : !rolePresent);
		
		ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>> createChangeRequestWrapped = idmIdentityController.changePermissions(test1.getId().toString());
		WorkflowTaskInstanceDto createChangeRequest = createChangeRequestWrapped.getBody().getResource();
		return createChangeRequest;
	}

	private Map<String, Object> createNewPermission(String roleName, Date validFrom, Date validTill) {
		Map<String, Object> role = new HashMap<>();
		Map<String, Object> roleEmbedded = new HashMap<>();
		Map<String, Object> roleIdEmbedded = new HashMap<>();
		roleEmbedded.put("role", roleIdEmbedded);
		roleIdEmbedded.put("id", idmRoleRepository.findOneByName(roleName).getId());
		role.put("validTill", validTill == null ? "" : sdf.format(validTill));
		role.put("validFrom", validFrom == null ? "" : sdf.format(validFrom));
		role.put("_embedded", roleEmbedded);
	
		return role;
	}
	
	private Map<String, Object> createChangePermission(Long identityRoleId, Date validFrom, Date validTill) {
		Map<String, Object> role = new HashMap<>();
		
		role.put("validTill", validTill == null ? "" : sdf.format(validTill));
		role.put("validFrom", validFrom == null ? "" : sdf.format(validFrom));
		role.put("id", identityRoleId);
	
		return role;
	}

}