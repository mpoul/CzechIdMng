package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WorkflowFilterDto {
	
	public static final String ORDER_ASC = "asc";
	public static final String ORDER_DESC = "desc";
	
	private int pageNumber = 0;
	private int pageSize = 10;
	private boolean sortAsc = false;
	private boolean sortDesc = false;
	private String sortByFields;
	
	private Map<String, Object> equalsVariables;
	private String processDefinitionId;
	private String processDefinitionKey;
	private String processInstanceId;
	private String superProcessInstanceId;
	private String name;
	private String id;
	private String category;

	public WorkflowFilterDto(int defaultPageSize) {
		this.pageSize = defaultPageSize;
	}
	
	public WorkflowFilterDto() {
	}
	
	public Map<String, Object> getEqualsVariables() {
		if (equalsVariables == null) {
			equalsVariables = new HashMap<>();
		}
		return equalsVariables;
	}

	public void setEqualsVariables(Map<String, Object> equalsVariables) {
		this.equalsVariables = equalsVariables;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public boolean isSortAsc() {
		return sortAsc;
	}

	public void setSortAsc(boolean sortAsc) {
		this.sortAsc = sortAsc;
	}

	public boolean isSortDesc() {
		return sortDesc;
	}

	public void setSortDesc(boolean sortDesc) {
		this.sortDesc = sortDesc;
	}

	public String getSortByFields() {
		return sortByFields;
	}

	public void setSortByFields(String sortByFields) {
		this.sortByFields = sortByFields;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String id) {
		this.processInstanceId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSuperProcessInstanceId() {
		return superProcessInstanceId;
	}

	public void setSuperProcessInstanceId(String superProcessInstanceId) {
		this.superProcessInstanceId = superProcessInstanceId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@JsonIgnore
	public void initSort(String sort) {
		if (sort == null) {
			this.setSortByFields(null);
			this.setSortAsc(true);
			this.setSortDesc(false);
			return;
		}
		String[] sorts = sort.split(",");
		if(sorts != null && sorts.length > 1){
			this.setSortByFields(sorts[0]);
			String order = sorts[sorts.length-1];
			if(WorkflowFilterDto.ORDER_ASC.equals(order)){
				this.setSortAsc(true);
			}
			if(WorkflowFilterDto.ORDER_DESC.equals(order)){
				this.setSortDesc(true);
			}
		}
		
	}
	
	

}
