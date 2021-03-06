package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Identity contract - working position
 * 
 * @author Radek Tomiška
 */
@Entity
@Table(name = "idm_identity_contract", indexes = {
		@Index(name = "idx_idm_identity_contract_gnt", columnList = "guarantee_id"),
		@Index(name = "idx_idm_identity_contract_idnt", columnList = "identity_id"),
		@Index(name = "idx_idm_identity_contract_wp", columnList = "working_position_id")})
public class IdmIdentityContract extends AbstractEntity implements ValidableEntity {

	private static final long serialVersionUID = 328041550861866181L;

	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentity identity;
	
	@Audited
	@Column(name = "valid_from")
	private LocalDate validFrom;
	
	@Audited
	@Column(name = "valid_till")
	private LocalDate validTill;
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = true)
	@JoinColumn(name = "guarantee_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentity guarantee;
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = true)
	@JoinColumn(name = "working_position_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmTreeNode workingPosition;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "position", length = DefaultFieldLengths.NAME)
	private String position; // string position - if working position tree is not configured, then this string could be used
	
	@Audited
	@NotNull
	@Column(name = "externe", nullable = false)
	private boolean externe;
	
	public IdmIdentityContract() {
	}
	
	public IdmIdentityContract(UUID id) {
		super(id);
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTo) {
		this.validTill = validTo;
	}

	public IdmIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}

	public IdmTreeNode getWorkingPosition() {
		return workingPosition;
	}

	public void setWorkingPosition(IdmTreeNode workingPosition) {
		this.workingPosition = workingPosition;
	}

	/**
	 * Manually defined  manager (if no tree structure is defined etc.)
	 * 
	 * @return
	 */
	public IdmIdentity getGuarantee() {
		return guarantee;
	}

	public void setGuarantee(IdmIdentity guarantee) {
		this.guarantee = guarantee;
	}
	
	/**
	 * Working position (if no tree structure is defined etc.)
	 * 
	 * @return
	 */
	public String getPosition() {
		return position;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
	
	/**
	 * Externe working position
	 * 
	 * @return
	 */
	public boolean isExterne() {
		return externe;
	}
	
	public void setExterne(boolean externe) {
		this.externe = externe;
	}
}
