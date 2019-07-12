package integration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.springframework.stereotype.Component;

import integration.persistence.PersistNode;

@Component
public class IntegrationConverter implements TypeConverters {
	public static final String DYNAMIC_KEY = "type";
	public static final String DYNAMIC_PROPS = "properties";
	public static final String DYNAMIC_TYPE = "Node";

	@Converter
	public PersistNode toPersistNode(Object o) {
		Map<?, ?> props=(Map<?, ?>) o;
		String type = (String) props.get(DYNAMIC_KEY);
		if (!DYNAMIC_TYPE.equalsIgnoreCase(type)) {
			throw new TypeNotPresentException(type, null);
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> properties = (Map<String, Object>) props.get(DYNAMIC_PROPS);
		return PersistNode.builder().type(type).properties(properties).build();
	}

	@Converter
	public List<PersistNode> toPersistNode(Map<?, ?>[] props) {
		PersistNode[] result = new PersistNode[props.length];
		for (int i = 0; i<props.length; i++) {
			 Map<?, ?> p= props[i];
			 result[i]=toPersistNode(p);
		}
		return Arrays.asList(result);
	}
}