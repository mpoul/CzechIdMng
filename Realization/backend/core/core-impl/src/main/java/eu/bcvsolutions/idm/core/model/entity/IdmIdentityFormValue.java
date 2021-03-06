package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;

/**
 * Identity extended attributes
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_identity_form_value", indexes = {
		@Index(name = "idx_sys_sys_form_a", columnList = "owner_id"),
		@Index(name = "idx_sys_sys_form_a_def", columnList = "attribute_id") })
public class IdmIdentityFormValue extends AbstractFormValue<IdmIdentity> {

	private static final long serialVersionUID = -6873566385389649927L;
	
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentity owner;
	
	public IdmIdentityFormValue() {
	}
	
	public IdmIdentityFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}
	
	@Override
	public IdmIdentity getOwner() {
		return owner;
	}
	
	public void setOwner(IdmIdentity owner) {
		this.owner = owner;
	}

}
