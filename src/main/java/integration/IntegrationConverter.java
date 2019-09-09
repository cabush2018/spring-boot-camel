package integration;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
		Map<String, Map<String, String>> obj = (Map<String, Map<String, String>>) o;
		return obj.entrySet().stream().findFirst().map(this::toPersistNode).orElseThrow(RuntimeException::new);
	}

	@SneakyThrows
	public Object toPersistNode(@NotNull Map.Entry<String, Map<String, String>> entry) {
		return toPersistent(entry.getKey(), entry.getValue());
		}

	@SneakyThrows
	public Object toPersistent(String entity, Map<String, ?> map) {
		final String[] location=entity.split("\\s|\\-|\\:|\\;|,|\\^|\\&|\\%|\\$|\\#|\\@|\\!");
		Object obj ;
		if (location.length>1) {
			String type = Arrays.asList(location).stream().collect(Collectors.joining("."));
			obj = Class.forName(type).getConstructor().newInstance();
			BeanUtils.populate(obj, map);
		} else {
			obj = PersistNode.builder().type(entity).properties(map).build();
		}
		return obj;
	}
}