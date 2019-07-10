package integration;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;

import integration.model.Thing;
import integration.persistence.PersistNode;

public class IntegrationConverter implements TypeConverters {
	public static final String DYNAMIC_KEY = "type";
	public static final String DYNAMIC_PROPS = "properties";
	public static final String DYNAMIC_TYPE = "Node";

	@Converter
	public Thing toThing(Map<String, ? extends Object> props) 
			throws IllegalAccessException, InvocationTargetException {
		Thing o = new Thing();
		org.apache.commons.beanutils.BeanUtils.populate(o, props);
		return o;
	}

	@Converter
	public PersistNode toPersistNode(Map<?, ?> props) {
		String type = (String) props.get(DYNAMIC_KEY);
		if (!DYNAMIC_TYPE.equalsIgnoreCase(type)) {
			throw new TypeNotPresentException(type, null);
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> properties = (Map<String, Object>) props.get(DYNAMIC_PROPS);
		PersistNode newNode = new PersistNode();
		newNode.setProperties(properties);
		newNode.setType(type);
		return newNode;
	}
}