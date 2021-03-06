package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.ParallelGatewayActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;
import eu.bcvsolutions.idm.security.api.service.SecurityService;

/**
 * Default implementation of workflow process historic service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultWorkflowHistoricProcessInstanceService implements WorkflowHistoricProcessInstanceService {

	private static final String DEFINITION_ID_DELIMITER = ":";

	@Autowired
	private HistoryService historyService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private WorkflowProcessDefinitionService definitionService;

	/**
	 * Search process history. Process variables will be included only for get
	 * specific process history. It means filter.processInstanceId is filled.
	 * 
	 * @param filter
	 * @return
	 */
	@Override
	public ResourcesWrapper<WorkflowHistoricProcessInstanceDto> search(WorkflowFilterDto filter) {
		String processDefinitionId = filter.getProcessDefinitionId();
		String processInstanceId = filter.getProcessInstanceId();

		Map<String, Object> equalsVariables = filter.getEqualsVariables();

		HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

		if (processInstanceId != null) {
			// Process variables will be included only for get by instance ID
			query.includeProcessVariables();
			query.processInstanceId(processInstanceId);
		}
		if (processDefinitionId != null) {
			query.processDefinitionId(processDefinitionId);
		}
		if (filter.getSuperProcessInstanceId() != null) {
			query.superProcessInstanceId(filter.getSuperProcessInstanceId());
		}
		if (filter.getProcessDefinitionKey() != null) {
			// For case when we have only process id, we will convert him to key
			query.processDefinitionKey(convertProcessIdToKey(filter.getProcessDefinitionKey()));
		}
		if (filter.getName() != null) {
			// with case sensitive
			query.variableValueLike(WorkflowHistoricProcessInstanceService.PROCESS_INSTANCE_NAME, "%" + filter.getName() + "%");
		}
		if (equalsVariables != null) {
			for (Entry<String, Object> entry : equalsVariables.entrySet()) {
				query.variableValueEquals(entry.getKey(), entry.getValue());
			}
		}
		// check security ... only involved user or applicant can work with
		// historic process instance
		// Applicant and Implementer is added to involved user after process
		// (subprocess) started. This modification allow not use OR clause.
		query.involvedUser(securityService.getUsername());

		if (WorkflowHistoricProcessInstanceService.SORT_BY_START_TIME.equals(filter.getSortByFields())) {
			query.orderByProcessInstanceStartTime();
		} else if (WorkflowHistoricProcessInstanceService.SORT_BY_END_TIME.equals(filter.getSortByFields())) {
			query.orderByProcessInstanceEndTime();
		} else {
			query.orderByProcessDefinitionId();
		}
		if (filter.isSortAsc()) {
			query.asc();
		}
		if (filter.isSortDesc()) {
			query.desc();
		}
		long count = query.count();
		List<HistoricProcessInstance> processInstances = query.listPage((filter.getPageNumber()) * filter.getPageSize(),
				filter.getPageSize());
		List<WorkflowHistoricProcessInstanceDto> dtos = new ArrayList<>();

		if (processInstances != null) {
			for (HistoricProcessInstance instance : processInstances) {
				dtos.add(toResource(instance));
			}
		}
		double totalPageDouble = ((double) count / filter.getPageSize());
		double totlaPageFlorred = Math.floor(totalPageDouble);
		long totalPage = 0;
		if (totalPageDouble > totlaPageFlorred) {
			totalPage = (long) (totlaPageFlorred + 1);
		}

		return new ResourcesWrapper<>(dtos, count, totalPage,
				filter.getPageNumber(), filter.getPageSize());
	}

	@Override
	public WorkflowHistoricProcessInstanceDto get(String historicProcessInstanceId) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(historicProcessInstanceId);
		filter.setSortAsc(true);
		Collection<WorkflowHistoricProcessInstanceDto> resources = this.search(filter).getResources();
		return !resources.isEmpty() ? resources.iterator().next() : null;
	}

	/**
	 * Generate diagram for process instance. Highlight historic path (activity
	 * and flows)
	 */
	@Override
	public InputStream getDiagram(String processInstanceId) {
		if (processInstanceId == null) {
			throw new ActivitiIllegalArgumentException("No process instance id provided");
		}

		HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();

		if (pi == null) {
			throw new ActivitiObjectNotFoundException(
					"Process instance with id " + processInstanceId + " could not be found", ProcessInstance.class);
		}

		ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
				.getDeployedProcessDefinition(pi.getProcessDefinitionId());

		if (pde != null && pde.isGraphicalNotationDefined()) {
			BpmnModel bpmnModel = repositoryService.getBpmnModel(pde.getId());
			List<String> historicActivityInstanceList = new ArrayList<String>();
			List<String> highLightedFlows = new ArrayList<String>();
			historicActivityInstanceList = getHighLightedFlows(pde, processInstanceId, historicActivityInstanceList,
					highLightedFlows);

			ProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();

			return diagramGenerator.generateDiagram(bpmnModel, "png", historicActivityInstanceList, highLightedFlows);

		} else {
			throw new ActivitiException(
					"Process instance with id " + processInstanceId + " has no graphic description");
		}
	}

	/**
	 * Convert process definition ID to process definition KEY.
	 * 
	 * @param processId
	 * @return
	 */
	private String convertProcessIdToKey(String processId) {
		if (processId == null || !processId.contains(DEFINITION_ID_DELIMITER)) {
			return processId;
		}
		return processId.split(DEFINITION_ID_DELIMITER)[0];
	}

	private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinition, String processInstanceId,
			List<String> historicActivityInstanceList, List<String> highLightedFlows) {

		List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(processInstanceId).orderByHistoricActivityInstanceEndTime().asc().list();

		for (HistoricActivityInstance hai : historicActivityInstances) {
			historicActivityInstanceList.add(hai.getActivityId());
		}

		// Check if is process still active
		boolean isProcessActive = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
				.active().count() > 0;
		List<String> currentHighLightedActivities = null;
		if (isProcessActive) {
			// add current activities to list
			currentHighLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
			historicActivityInstanceList.addAll(currentHighLightedActivities);
		}
		// activities and their sequence-flows
		getHighLightedFlows(processDefinition.getActivities(), historicActivityInstanceList, highLightedFlows);

		if (isProcessActive) {
			return currentHighLightedActivities;
		}
		return historicActivityInstanceList;
	}

	/**
	 * Add highlight flows by historic activity list
	 * 
	 * @param activityList
	 * @param historicActivityInstanceList
	 * @param highLightedFlows
	 */
	private void getHighLightedFlows(List<ActivityImpl> activityList, List<String> historicActivityInstanceList,
			List<String> highLightedFlows) {
		Map<Integer, String> usedActivityFlow = new HashMap<Integer, String>();
		/**
		 * Iterate all used activity (start to end)
		 */
		for (int i = 0; i < historicActivityInstanceList.size(); i++) {
			String activityId = historicActivityInstanceList.get(i);
			ActivityImpl currentActivity = null;
			for (ActivityImpl activity : activityList) {
				if (activityId.equals(activity.getId())) {
					currentActivity = activity;
					break;
				}
			}
			if (currentActivity == null) {
				continue;
			}
			/**
			 * Get incoming transitions from current activity 
			 */
			List<PvmTransition> pvmTransitionList = currentActivity.getIncomingTransitions();
			boolean findedFlow = false;
			// create index previous activity
			int prevIndex = i - 1;
			/**
			 * We will finding flow for highlight. We will start with previous activity. 
			 * if we find nothing, then we will continuing with previous activity (index = index -1).
			 */
			while (!findedFlow) {
				if (prevIndex < 0) {
					// We are on begin .. nothing to highlight
					break;
				}
				String tempActivity = historicActivityInstanceList.get(prevIndex);
				for (PvmTransition pvmTransition : pvmTransitionList) {
					String destinationFlowId = pvmTransition.getSource().getId();
					if (tempActivity != null && destinationFlowId.equals(tempActivity)) {
						highLightedFlows.add(pvmTransition.getId());
						findedFlow = true;
						for (ActivityImpl activity : activityList) {
							if (tempActivity.equals(activity.getId())
									&& !(activity.getActivityBehavior() instanceof ParallelGatewayActivityBehavior)) {
								// We use activity in other cycle if is ParallelGate. 
								// Its means, we don't put parralel gate to usedActivityFlow map.
								usedActivityFlow.put(prevIndex, tempActivity);
							}
						}

					}
				}
				if (!findedFlow) {
					// If we don't find flow for highlight, we have to continue with previous historic activity
					while (true) {
						prevIndex = prevIndex - 1;
						if (!usedActivityFlow.containsKey(prevIndex)) {
							break;
						}
					}
				}
			}
		}
	}

	private WorkflowHistoricProcessInstanceDto toResource(HistoricProcessInstance instance) {
		if (instance == null) {
			return null;
		}

		String instanceName = instance.getName();
		// If we don't have process name, then we try variable with key
		// processInstanceName
		if (instanceName == null && instance.getProcessVariables() != null && instance.getProcessVariables()
				.containsKey(WorkflowHistoricProcessInstanceService.PROCESS_INSTANCE_NAME)) {
			instanceName = (String) instance.getProcessVariables()
					.get(WorkflowHistoricProcessInstanceService.PROCESS_INSTANCE_NAME);
		}
		// If still don't have process name, then we try load variable name from
		// historic variables
		if (instanceName == null || instanceName.isEmpty()) {
			HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery()
					.processInstanceId(instance.getId())
					.variableName(WorkflowHistoricProcessInstanceService.PROCESS_INSTANCE_NAME).singleResult();
			instanceName = variableInstance != null ? (String) variableInstance.getValue() : null;
		}
		if (instanceName == null || instanceName.isEmpty()) {
			instanceName = definitionService.getById(instance.getProcessDefinitionId()).getName();
		}

		WorkflowHistoricProcessInstanceDto dto = new WorkflowHistoricProcessInstanceDto();
		dto.setId(instance.getId());
		dto.setName(instanceName);
		dto.setProcessDefinitionId(instance.getProcessDefinitionId());
		dto.setProcessVariables(instance.getProcessVariables());
		dto.setDeleteReason(instance.getDeleteReason());
		dto.setDurationInMillis(instance.getDurationInMillis());
		dto.setEndTime(instance.getEndTime());
		dto.setStartActivityId(instance.getStartActivityId());
		dto.setStartTime(instance.getStartTime());
		dto.setStartUserId(instance.getStartUserId());
		dto.setSuperProcessInstanceId(instance.getSuperProcessInstanceId());

		return dto;
	}

}
