package integration;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Component;

import integration.persistence.PersistNode;
import lombok.SneakyThrows;

@Component
@SuppressWarnings("unchecked")
public class IntegrationConverter implements TypeConverters {

	@Converter
	public Object toPersistNode(@NotNull Object o) {
		Map<String, Map<String, Object>> obj = (Map<String, Map<String, Object>>) o;
		return obj.entrySet().stream().findFirst().map(this::toPersistNode).get();
	}

	@SneakyThrows
	public Object toPersistNode(@NotNull Map.Entry<String, Map<String, Object>> entry) {
		return toPersistent(entry.getKey(), entry.getValue());
		}

	@Converter @Deprecated
	public PersistNode toPersistNode(@NotBlank String type, @NotNull Object o) {
		Map<String, Object> props = (Map<String, Object>) o;
		return PersistNode.builder().type(type).properties(props).build();
	}

	public Object toPersistent(String entity, Map<String, Object> properties)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException {
		final String[] location=entity.split("\\s|\\-|\\:|\\;|,|\\^|\\&|\\%|\\$|\\#|\\@|\\!");
		Object obj ;
		if (location.length>1) {
			String type = Arrays.asList(location).stream().collect(Collectors.joining("."));
			obj = Class.forName(type).newInstance();
			BeanUtils.populate(obj, properties);
		} else {
			obj = PersistNode.builder().type(entity).properties(properties).build();
		}
		return obj;
	}


	@Converter @Deprecated
	public List<Object> toPersistNode(@NotNull Map<?, ?>[] props) {
		Object[] result = new PersistNode[props.length];
		for (int i = 0; i < props.length; i++) {
			Map<?, ?> p = props[i];
			result[i] = toPersistNode(p);
		}
		return Arrays.asList(result);
	}
}