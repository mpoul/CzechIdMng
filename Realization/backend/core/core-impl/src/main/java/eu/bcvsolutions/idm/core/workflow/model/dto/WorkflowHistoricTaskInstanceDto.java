package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowHistoricTaskInstanceDto {

	private String processDefinitionId;
	private String id;
	private String name;
	/** The time the process was started. */
	private Date startTime;
	/** The time the process was ended. */
	private Date endTime;
	/**
	 * The difference between {@link #getEndTime()} and {@link #getStartTime()}
	 */
	private Long durationInMillis;
	/**
	 * The authenticated user that started this process instance.
	 * 
	 * @see IdentityService#setAuthenticatedUserId(String)
	 */
	private String startUserId;
	/** Obtains the reason for the process instance's deletion. */
	private String deleteReason;
	private int priority;
	private String assignee;
	private Date createTime;
	private Date dueDate;
	private String completeTaskDecision;
	private Map<String, Object> taskVariables;
	private List<String> candicateUsers;

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Long getDurationInMillis() {
		return durationInMillis;
	}

	public void setDurationInMillis(Long durationInMillis) {
		this.durationInMillis = durationInMillis;
	}

	public String getStartUserId() {
		return startUserId;
	}

	public void setStartUserId(String startUserId) {
		this.startUserId = startUserId;
	}

	public String getDeleteReason() {
		return deleteReason;
	}

	public void setDeleteReason(String deleteReason) {
		this.deleteReason = deleteReason;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getCompleteTaskDecision() {
		return completeTaskDecision;
	}

	public void setCompleteTaskDecision(String completeTaskDecision) {
		this.completeTaskDecision = completeTaskDecision;
	}

	public Map<String, Object> getTaskVariables() {
		if(taskVariables == null){
			taskVariables = new HashMap<>();
		}
		return taskVariables;
	}

	public void setTaskVariables(Map<String, Object> taskVariables) {
		this.taskVariables = taskVariables;
	}
	
	public List<String> getCandicateUsers() {
		return candicateUsers;
	}

	public void setCandicateUsers(List<String> candicateUsers) {
		this.candicateUsers = candicateUsers;
	}

}
