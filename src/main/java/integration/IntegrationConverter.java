package integration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Component;

import integration.model.Concept;
import integration.persistence.PersistNode;
import lombok.SneakyThrows;

@Component
public class IntegrationConverter implements TypeConverters {
	public static final String DYNAMIC_KEY = "type";
	public static final String DYNAMIC_PROPS = "properties";
	public static final String DYNAMIC_TYPE = "Node";

	@Converter
	@SneakyThrows
	@SuppressWarnings("unchecked")
	public Concept toConcept(Object o) {
		Concept result = Concept.class.newInstance();
		BeanUtils.populate(result, (Map<String, Object>) o);
		return result;
	}

//	@Converter
//	public PersistNode toPersistNode(Object o) {
//		Map<?, ?> props=(Map<?, ?>) o;
//		String type = (String) props.get(DYNAMIC_KEY);
//		if (!DYNAMIC_TYPE.equalsIgnoreCase(type)) {
//			throw new TypeNotPresentException(type, null);
//		}
//		@SuppressWarnings("unchecked")
//		Map<String, Object> properties = (Map<String, Object>) props.get(DYNAMIC_PROPS);
//		return PersistNode.builder().type(type).properties(properties).build();
//	}

	@Converter
	public PersistNode toPersistNode(Object o) {
		Map<String, Map<String,Object>> obj=(Map<String, Map<String,Object>>) o;
		for (Map.Entry<String, Map<String,Object>> entry: obj.entrySet()) {
			return toPersistNode(entry.getKey(), entry.getValue());
		}
		return null;
	}

	@Converter
	public PersistNode toPersistNode(String type, Object o) {
		@SuppressWarnings("unchecked")
		Map<String,Object> props=(Map<String,Object>) o;
		return PersistNode.builder().type(type).properties(props).build();
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