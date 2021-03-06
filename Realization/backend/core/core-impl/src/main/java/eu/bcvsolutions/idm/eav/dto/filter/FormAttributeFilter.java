package eu.bcvsolutions.idm.eav.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;

/**
 * Form attribute definition filter
 * 
 * @author Radek Tomiška
 *
 */
public class FormAttributeFilter implements BaseFilter {

	private IdmFormDefinition formDefinition;
	private String name;

	public IdmFormDefinition getFormDefinition() {
		return formDefinition;
	}

	public void setFormDefinition(IdmFormDefinition formDefinition) {
		this.formDefinition = formDefinition;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
