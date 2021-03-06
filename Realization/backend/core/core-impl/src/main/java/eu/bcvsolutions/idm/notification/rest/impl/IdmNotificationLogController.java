	package eu.bcvsolutions.idm.notification.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.notification.domain.NotificationState;
import eu.bcvsolutions.idm.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.notification.service.api.NotificationLogService;

/**
 * Read and send notifications 
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/notifications")
public class IdmNotificationLogController extends AbstractReadWriteEntityController<IdmNotificationLog, NotificationFilter> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmNotificationLogController.class);
	private final NotificationLogService notificationLogService;
	
	@Autowired
	public IdmNotificationLogController(
			EntityLookupService entityLookupService,
			NotificationLogService notificationLogService) {
		super(entityLookupService);
		//
		Assert.notNull(notificationLogService);
		//
		this.notificationLogService = notificationLogService;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters, @PageableDefault Pageable pageable,
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable, PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return super.get(backendId, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_WRITE + "')")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	/**
	 * Send notification
	 * 
	 * @param notificationLog
	 */
	@Override
	public IdmNotificationLog createEntity(IdmNotificationLog entity) {
		LOG.debug("Notification log [{}] was created and notificatio will be send.", entity);
		notificationLogService.send(entity);
		// TODO: send method should result notification or ex to prevent another loading
		return getEntity(entity.getId());
	}
	
	@Override
	protected NotificationFilter toFilter(MultiValueMap<String, Object> parameters) {
		NotificationFilter filter = new NotificationFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setSender(getParameterConverter().toString(parameters, "sender"));
		filter.setRecipient(getParameterConverter().toString(parameters, "recipient"));
		filter.setFrom(getParameterConverter().toDateTime(parameters, "from"));
		filter.setTill(getParameterConverter().toDateTime(parameters, "till"));
		filter.setState(getParameterConverter().toEnum(parameters, "state", NotificationState.class));		
		return filter;
	}
}
