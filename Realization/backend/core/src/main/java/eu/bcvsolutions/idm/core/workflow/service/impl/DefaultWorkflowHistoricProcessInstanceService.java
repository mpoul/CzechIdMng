package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.security.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Default implementation of workflow process historic service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultWorkflowHistoricProcessInstanceService implements WorkflowHistoricProcessInstanceService {

	@Autowired
	private HistoryService historyService;

	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private RepositoryService repositoryService;


	@Override
	public ResourcesWrapper<WorkflowHistoricProcessInstanceDto> search(WorkflowFilterDto filter) {
		String processDefinitionId = filter.getProcessDefinitionId();
		String processInstanceId = filter.getProcessInstanceId();

		Map<String, Object> equalsVariables = filter.getEqualsVariables();

		HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

		query.includeProcessVariables();

		if (processInstanceId != null) {
			query.processInstanceId(processInstanceId);
		}
		if (processDefinitionId != null) {
			query.processDefinitionId(processDefinitionId);
		}
		if (filter.getProcessDefinitionKey() != null) {
			query.processDefinitionKey(filter.getProcessDefinitionKey());
		}
		if (equalsVariables != null) {
			for (String key : equalsVariables.keySet()) {
				query.variableValueEquals(key, equalsVariables.get(key));
			}
		}
		// check security ... only involved user or applicant can work with
		// historic process instance
		query.or();
		query.involvedUser(securityService.getUsername());
		query.variableValueEquals(WorkflowProcessInstanceService.APPLICANT_USERNAME,
				securityService.getOriginalUsername());
		query.endOr();

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

		ResourcesWrapper<WorkflowHistoricProcessInstanceDto> result = new ResourcesWrapper<>(dtos, count, totalPage,
				filter.getPageNumber(), filter.getPageSize());
		return result;
	}

	@Override
	public WorkflowHistoricProcessInstanceDto get(String historicProcessInstanceId) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(historicProcessInstanceId);
		filter.setSortAsc(true);
		ResourcesWrapper<WorkflowHistoricProcessInstanceDto> resource = this.search(filter);
		return resource.getResources() != null ? resource.getResources().iterator().next() : null;
	}

	@Override
	/**
	 * Generate diagram for process instance. Highlight historic path (activity and flows)
	 */
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

			InputStream resource = diagramGenerator.generateDiagram(bpmnModel, "png", historicActivityInstanceList,
					highLightedFlows);
			return resource;

		} else {
			throw new ActivitiException(
					"Process instance with id " + processInstanceId + " has no graphic description");
		}
	}

	private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinition, String processInstanceId,
			List<String> historicActivityInstanceList, List<String> highLightedFlows) {

		List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();

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

	private void getHighLightedFlows(List<ActivityImpl> activityList, List<String> historicActivityInstanceList,
			List<String> highLightedFlows) {
		ActivityImpl prevActivity = null;
		for (String activityId : historicActivityInstanceList) {
			ActivityImpl currentActivity = null;
			for (ActivityImpl activity : activityList) {
				if (activityId.equals(activity.getId())) {
					currentActivity = activity;
				}
			}

			List<PvmTransition> pvmTransitionList = currentActivity.getIncomingTransitions();
			for (PvmTransition pvmTransition : pvmTransitionList) {
				String destinationFlowId = pvmTransition.getSource().getId();
				if (prevActivity != null && destinationFlowId.equals(prevActivity.getId())) {
					highLightedFlows.add(pvmTransition.getId());
				}
			}

			prevActivity = currentActivity;
		}
	}

	private WorkflowHistoricProcessInstanceDto toResource(HistoricProcessInstance instance) {
		if (instance == null) {
			return null;
		}

		WorkflowHistoricProcessInstanceDto dto = new WorkflowHistoricProcessInstanceDto();
		dto.setId(instance.getId());
		dto.setName(instance.getName());
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
