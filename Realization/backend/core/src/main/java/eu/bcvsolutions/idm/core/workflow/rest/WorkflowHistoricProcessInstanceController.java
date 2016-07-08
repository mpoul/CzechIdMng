package eu.bcvsolutions.idm.core.workflow.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;

/**
 * Rest controller for workflow historic instance processes
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = "/api/workflow/history/processes/")
public class WorkflowHistoricProcessInstanceController {

	@Autowired
	private WorkflowHistoricProcessInstanceService workflowHistoricProcessInstanceService;

	/**
	 * Search historic instances of processes with same variables and for logged user
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "search/")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowHistoricProcessInstanceDto>>> search(
			@RequestBody WorkflowFilterDto filter) {
		ResourcesWrapper<WorkflowHistoricProcessInstanceDto> result = workflowHistoricProcessInstanceService.search(filter);;
		List<WorkflowHistoricProcessInstanceDto> processes = (List<WorkflowHistoricProcessInstanceDto>) result.getResources();
		List<ResourceWrapper<WorkflowHistoricProcessInstanceDto>> wrappers = new ArrayList<>();
		
		for (WorkflowHistoricProcessInstanceDto process : processes) {
			wrappers.add(new ResourceWrapper<WorkflowHistoricProcessInstanceDto>(process));
		}
		ResourcesWrapper<ResourceWrapper<WorkflowHistoricProcessInstanceDto>> resources = new ResourcesWrapper<ResourceWrapper<WorkflowHistoricProcessInstanceDto>>(
				wrappers);
		resources.setPage(result.getPage());
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowHistoricProcessInstanceDto>>>(resources, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "search/quick")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowHistoricProcessInstanceDto>>> searchQuick(
			@RequestParam int size, @RequestParam int page, @RequestParam String sort) {

		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setPageNumber(page);
		filter.setPageSize(size);
		filter.initSort(sort);
		
		return this.search(filter);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "{historicProcessInstanceId}")
	public ResponseEntity<ResourceWrapper<WorkflowHistoricProcessInstanceDto>> get(@PathVariable String historicProcessInstanceId) {
		ResourceWrapper<WorkflowHistoricProcessInstanceDto> resource = new ResourceWrapper<WorkflowHistoricProcessInstanceDto>(
				workflowHistoricProcessInstanceService.get(historicProcessInstanceId));
		return new ResponseEntity<ResourceWrapper<WorkflowHistoricProcessInstanceDto>>(resource, HttpStatus.OK);
	}


}