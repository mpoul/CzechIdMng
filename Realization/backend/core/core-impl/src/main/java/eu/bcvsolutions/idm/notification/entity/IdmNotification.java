package eu.bcvsolutions.idm.notification.entity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.notification.domain.BaseNotification;

@Entity
@Table(name = "idm_notification", indexes = {
		@Index(name = "idx_idm_notification_sender", columnList = "sender_id"),
		@Index(name = "idx_idm_notification_parent", columnList = "parent_notification_id")
		})
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class IdmNotification extends AbstractEntity implements BaseNotification {

	private static final long serialVersionUID = -2038771692205141212L;

	@Embedded
	private IdmMessage message;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "sender_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentity sender;
	
	@JsonManagedReference
	@OneToMany(mappedBy = "notification", cascade = CascadeType.ALL) // orphan removal is not necessary - notification can be added only
	private List<IdmNotificationRecipient> recipients;
	
	@JsonProperty(access = Access.READ_ONLY)
	@Column(name = "sent")
	private DateTime sent;
	
	@JsonProperty(access = Access.READ_ONLY)
	@Size(max = DefaultFieldLengths.LOG)
	@Column(name = "SENT_LOG", length = DefaultFieldLengths.LOG)
	private String sentLog;
	
	@JsonIgnore
	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_notification_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmNotification parent;
	
	public void setSender(IdmIdentity sender) {
		this.sender = sender;
	}
	
	@Override
	public IdmIdentity getSender() {
		return sender;
	}

	public void setRecipients(List<IdmNotificationRecipient> recipients) {
		this.recipients = recipients;
	}
	
	public List<IdmNotificationRecipient> getRecipients() {
		if (recipients == null) {
			recipients = new ArrayList<>();
		}
		return recipients;
	}

	public String getSentLog() {
		return sentLog;
	}

	public void setSentLog(String sentLog) {
		this.sentLog = sentLog;
	}
	
	public void setMessage(IdmMessage message) {
		this.message = message;
	}
	
	public IdmMessage getMessage() {
		return message;
	}
	
	public void setSent(DateTime sent) {
		this.sent = sent;
	}
	
	public DateTime getSent() {
		return sent;
	}
	
	public void setParent(IdmNotification parent) {
		this.parent = parent;
	}
	
	public IdmNotification getParent() {
		return parent;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("Notification [id:{2}] [message:{0}] [first recipient:{1}]", getMessage(), getRecipients().isEmpty() ? null : getRecipients().get(0), getId());
	}
}
