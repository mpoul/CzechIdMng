package eu.bcvsolutions.idm.notification.service.impl;

import java.util.Date;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.notification.service.NotificationLogService;

@Component("notificationService")
public class DefaultNotificationService extends AbstractNotificationService implements NotificationLogService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractNotificationService.class);
	
	@Autowired
	private IdmNotificationLogRepository idmNotificationRepository;
	
	@Autowired
    private ProducerTemplate producerTemplate;
	
	@Override
	public boolean send(IdmNotification notification) {
		Assert.notNull(notification, "Noticition is required!");
		//
		IdmNotificationLog notificationLog = (IdmNotificationLog) createLog(notification);
		return sendNotificationLog(notificationLog);
	}
	
	/**
	 * Sends existing notification to routing
	 * 
	 * @param notification
	 * @return
	 */
	@Override
	public boolean sendNotificationLog(IdmNotificationLog notificationLog) {
		log.info("Sending notification [{}]", notificationLog);
		// send notification to routing
		producerTemplate.sendBody("direct:notifications", notificationLog);
		return true;
	}
	
	/**
	 * Persists new notification record from given notification
	 * 
	 * @param notification
	 * @return
	 */
	private IdmNotification createLog(IdmNotification notification) {
		Assert.notNull(notification);
		Assert.notNull(notification.getMessage());
		// we can only create log, if notification is instance of IdmNotificationLog
		if (notification instanceof IdmNotificationLog) {
			notification.setSent(new Date());
			return idmNotificationRepository.save((IdmNotificationLog) notification);
		}
		// we need to clone notification
		IdmNotificationLog notificationLog = new IdmNotificationLog();
		notificationLog.setSent(new Date());
		// clone message
		notificationLog.setMessage(cloneMessage(notification));
		// clone recipients
		notification.getRecipients().forEach(recipient -> {
			notificationLog.getRecipients().add(cloneRecipient(notificationLog, recipient));
		});
		// clone from - resolve real email
		if (notification.getSender() != null) {
			notificationLog.setSender(cloneRecipient(notificationLog, notification.getSender()));
		}
		return idmNotificationRepository.save(notificationLog);
	}
}