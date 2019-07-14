package integration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.springframework.stereotype.Component;

import integration.persistence.PersistNode;

@Component
@SuppressWarnings("unchecked")
public class IntegrationConverter implements TypeConverters {

	@Converter
	public PersistNode toPersistNode(Object o) {
		Map<String, Map<String, Object>> obj = (Map<String, Map<String, Object>>) o;
		return obj.entrySet().stream().findFirst().map(this::toPersistNode).get();
	}

	public PersistNode toPersistNode(Map.Entry<String, Map<String, Object>> entry) {
		return toPersistNode(entry.getKey(), entry.getValue());
	}

	@Converter
	public PersistNode toPersistNode(String type, Object o) {
		Map<String, Object> props = (Map<String, Object>) o;
		return PersistNode.builder().type(type).properties(props).build();
	}

	@Converter
	public List<PersistNode> toPersistNode(Map<?, ?>[] props) {
		PersistNode[] result = new PersistNode[props.length];
		for (int i = 0; i < props.length; i++) {
			Map<?, ?> p = props[i];
			result[i] = toPersistNode(p);
		}
		return Arrays.asList(result);
	}
}