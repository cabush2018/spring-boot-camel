package integration.persistence;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import integration.IntegrationConverter;

@Service
@Transactional
public class PersistenceService {

	@Value("${app.unmapped-entities:true}")
	private boolean processUnmapped;

	@Value("${app.mapped-entities:true}")
	private boolean processMapped;

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private IntegrationConverter converter;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object persist(@NotNull Object in) {
		if (processMapped && isMapped(in.getClass())) {
			persistMapped(in);
		} else if (processUnmapped && in instanceof PersistNode) {
			persistUnmapped((PersistNode) in);
		} else if (in instanceof List) {
			((List) in).stream().map(converter::toPersistNode).forEach(this::persist);
		} else if (in instanceof Map) {
			((Map<String, Map<String, Object>>) in).entrySet().stream().forEach(this::persistEntry);
		} else {
			throw new TypeNotPresentException(in.getClass().getCanonicalName(), null);
		}
		return in;
	}

	public Object persistEntry(@NotNull Map.Entry<String, Map<String, Object>> e) {
		return this.persist(e.getKey(), e.getValue());
	}

	public Object persist(@NotBlank String entity, @NotNull Map<String, Object> properties) {
		return this.persist(converter.toPersistent(entity, properties));
	}

	@Cacheable
	public final boolean isMapped(Class<?> clazz) {
		return em.getMetamodel().getEntities().parallelStream().map(EntityType::getJavaType).anyMatch(clazz::equals);
	}

	private Object persistUnmapped(PersistNode in) {
		return update(in) || insert(in) ? in : null;
	}

	private Object persistMapped(Object in) {
		return em.merge(in);
	}

	private boolean insert(PersistNode in) {
		Set<Entry<String, Object>> inSet = in.getProperties().entrySet();
		String columns = inSet.stream().map(Map.Entry<String, Object>::getKey).collect(Collectors.joining(","));
		String values = inSet.stream().map((Map.Entry<?, ?> e) -> formatSql(e.getValue()))
				.collect(Collectors.joining(","));
		return executeQuery(String.format("INSERT INTO %s (%s) VALUES ( %s )", in.getType(), columns, values)) > 0;
	}

	private boolean update(PersistNode in) {
		Set<Entry<String, Object>> inSet = in.getProperties().entrySet();
		String pairs = inSet.stream()
				.map((Map.Entry<?, ?> e) -> String.format(" %s = %s", e.getKey(), formatSql(e.getValue())))
				.collect(Collectors.joining(","));
		String id = inSet.stream().filter((Map.Entry<String, Object> e) -> "id".equalsIgnoreCase(e.getKey()))
				.findFirst().map((Map.Entry<?, ?> e) -> "id = " + e.getValue()).orElseThrow(RuntimeException::new);
		return executeQuery(String.format("UPDATE %s SET %s WHERE %s", in.getType(), pairs, id)) > 0;
	}

	private int executeQuery(String sql) {
		return em.createNativeQuery(sql).executeUpdate();
	}

	private String formatSql(Object e) {
		String value = null;
		if (e == null || e instanceof Date || e instanceof String) {
			value = String.format("'%s'", e);
		} else if (e instanceof Number) {
			value = e.toString();
		} else if (e instanceof Boolean) {
			value = (boolean) e ? "1" : "0";
		} else {
			throw new TypeNotPresentException(e.getClass().getCanonicalName(), null);
		}
		return value;
	}
}
