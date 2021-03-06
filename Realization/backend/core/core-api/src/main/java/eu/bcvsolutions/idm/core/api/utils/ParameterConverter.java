package eu.bcvsolutions.idm.core.api.utils;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;

/**
 * Rest controller helpers
 * - parameters converters
 * 
 * @author Radek Tomiška
 *
 */
public class ParameterConverter {

	private final EntityLookupService entityLookupService;
	private final ObjectMapper mapper;
	
	public ParameterConverter(
			EntityLookupService entityLookupService,
			ObjectMapper mapper) {
		Assert.notNull(entityLookupService);
		Assert.notNull(mapper);
		//
		this.entityLookupService = entityLookupService;
		this.mapper = mapper;
	}
	
	/**
	 * Converts http get parameters to filter
	 * 
	 * @param parameters
	 * @param filterClass
	 * @return
	 */
	public <F extends BaseFilter> F toFilter(MultiValueMap<String, Object> parameters, Class<F> filterClass) {
		if (mapper == null || parameters.isEmpty()) {
			return null;
		}
		return mapper.convertValue(parameters.toSingleValueMap(), filterClass);
	}
	
	/**
	 * Reads {@code String} parameter from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public String toString(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters);
	    Assert.notNull(parameterName);
	    //
		return (String)parameters.toSingleValueMap().get(parameterName);
	}
	
	/**
	 * Converts parameter to {@code Boolean} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public Boolean toBoolean(MultiValueMap<String, Object> parameters, String parameterName) {
		String valueAsString = toString(parameters, parameterName);
		if (StringUtils.isNotEmpty(valueAsString)) {
			return Boolean.valueOf(valueAsString);
		}
		return null;
	}
	
	/**
	 * Converts parameter to {@code Long} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public Long toLong(MultiValueMap<String, Object> parameters, String parameterName) {
		String valueAsString = toString(parameters, parameterName);
		if(StringUtils.isNotEmpty(valueAsString)) {
			try {
				return Long.valueOf(valueAsString);
			} catch (NumberFormatException ex) {
				throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of(parameterName, valueAsString), ex);
			}		
		}
		return null;
	}
	
	/**
	 * Converts parameter to {@code UUID} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public UUID toUuid(MultiValueMap<String, Object> parameters, String parameterName) {
		String valueAsString = toString(parameters, parameterName);
		if(StringUtils.isNotEmpty(valueAsString)) {
			try {
				return UUID.fromString(valueAsString);
			} catch (IllegalArgumentException ex) {
				throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of(parameterName, valueAsString), ex);
			}		
		}
		return null;
	}
	
	/**
	 * Converts parameter to given {@code enumClass} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param enumClass
	 * @return
	 */
	public <T extends Enum<T>> T toEnum(MultiValueMap<String, Object> parameters, String parameterName, Class<T> enumClass) {
		Assert.notNull(enumClass);
	    //
	    String valueAsString = toString(parameters, parameterName);
	    if(StringUtils.isEmpty(valueAsString)) {
	    	return null;
	    }
        try {
            return Enum.valueOf(enumClass, valueAsString.trim().toUpperCase());
        } catch(IllegalArgumentException ex) {
        	throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of(parameterName, valueAsString), ex);
        }
	}
	
	/**
	 * Converts parameter to given {@code entityClass} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param entityClass
	 * @return
	 */
	public <T extends BaseEntity> T toEntity(MultiValueMap<String, Object> parameters, String parameterName, Class<T> entityClass) {
		 String valueAsString = toString(parameters, parameterName);
	    if(StringUtils.isEmpty(valueAsString)) {
	    	return null;
	    }
		T entity = entityLookupService.lookup(entityClass, valueAsString);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE, "Entity type [%s] with identifier [%s] does not found", ImmutableMap.of("entityClass", entityClass.getSimpleName(), parameterName, valueAsString));
		}
		return entity;
	}
	
	/**
	 * Converts parameter to given {@code entityClass} from given parameters.
	 * 
	 * @param parameterValue
	 * @param parameterName
	 * @param entityClass
	 * @return
	 */
	public <T extends BaseEntity> T toEntity(String parameterValue, Class<T> entityClass) {
	    if(StringUtils.isEmpty(parameterValue)) {
	    	return null;
	    }
		T entity = entityLookupService.lookup(entityClass, parameterValue);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE, "Entity type [%s] with identifier [%s] does not found", ImmutableMap.of("entityClass", entityClass.getSimpleName(), "identifier", parameterValue));
		}
		return entity;
	}
	
	/**
	 * Converts parameter {@code DateTime} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public DateTime toDateTime(MultiValueMap<String, Object> parameters, String parameterName) {
		String valueAsString = toString(parameters, parameterName);
		if (valueAsString == null || valueAsString.isEmpty()) {
			return null;
		} else {
			return new DateTime(valueAsString);
		}
	}
}
