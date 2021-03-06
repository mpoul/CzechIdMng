package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Event state holder (content + metadata)
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public abstract class AbstractEntityEvent<E extends BaseEntity> extends ApplicationEvent implements EntityEvent<E> {

	private static final long serialVersionUID = 2309175762418747517L;
	private final EventType type;
	private final Map<String, Serializable> properties = new LinkedHashMap<>();
	private final EventContext<E> context;
	
	public AbstractEntityEvent(EventType type, E content, Map<String, Serializable> properties, EventContext<E> context) {
		super(content);
		//
		Assert.notNull(type, "Operation is required!");
		//
		this.type = type;
		if (properties != null) {
			this.properties.putAll(properties);
		}
		this.context = context == null ? new DefaultEventContext<>() : context;
	}
	
	public AbstractEntityEvent(EventType type, E content, Map<String, Serializable> properties) {
		this(type, content, properties, null);
	}

	public AbstractEntityEvent(EventType type, E content) {
		this(type, content, null, null);
	}

	@Override
	public EventType getType() {
		return type;
	}

	@Override
	@SuppressWarnings("unchecked")
	public E getContent() {
		return (E) getSource();
	}
	
	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}
	
	@Override
	public EventContext<E> getContext() {
		return context;
	}
	
	/**
	 * Event is closed = no other events will be processed (break event chain)
	 * 
	 * @return
	 */
	@Override
	public boolean isClosed() {
		return context.isClosed();
	}
	
	@Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass().getSuperclass(), ResolvableType.forInstance(getContent()));
    }
}
