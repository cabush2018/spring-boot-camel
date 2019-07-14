package integration.persistence;

import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.ConversionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import integration.IntegrationConverter;
import lombok.Setter;

@Service
@Transactional
@ConfigurationProperties(prefix = "app")
public class PersistenceService {
	
	@Setter
	private Map<String, Map<?, ?>> mappings;
	
	@Value("${app.unmapped-entities}")
	private boolean processUnmapped;

	@Value("${app.mapped-entities}")
	private boolean processMapped;

	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private IntegrationConverter converter;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void persist(@NotNull Object in) {
		if ( isMapped(in) ) {
			if (processMapped) {
				persistMapped(in);
			}
		} else if ( in instanceof PersistNode ) {
			if ( processUnmapped ) {
				persistUnmapped((PersistNode) in);
			}
		} else if (in instanceof List) {
			((List)in).stream().map(converter::toPersistNode).forEach(this::persist);
		}  else if (in instanceof Map) {
			((Map<String,Map<String,Object>>)in).entrySet().stream()
				.forEach(this::persistEntry);
		} else {
			throw new TypeNotPresentException(in.getClass().getCanonicalName(), null);
		}
	}

	public void persistEntry(@NotNull Map.Entry<String,Map<String,Object>> e) {
		this.persist(e.getKey(),e.getValue());
	}

	public void persist(@NotBlank String entity, @NotNull Map<String, Object> properties) {
		try {
			Object obj = converter.toPersistent(entity, properties);
			this.persist(obj);
		} catch (IllegalAccessException | InvocationTargetException | 
				InstantiationException | ClassNotFoundException e) {
			throw new ConversionException(e);		
		}
	}

	private boolean isMapped(Object in) {
		return em.getMetamodel().getEntities().parallelStream().map(EntityType::getJavaType).map(Class::getSimpleName)
				.anyMatch(in.getClass().getSimpleName()::equals);
	}

	private void persistUnmapped(PersistNode in) {
		if (update(in)) {
			return;
		}
		insert(in);
	}

	private void persistMapped(Object in) {
		em.merge(in);
	}

	private void insert(PersistNode in) {
		String columns = in.getProperties().keySet().stream().collect(Collectors.joining(","));
		String values = in.getProperties().entrySet().stream().map((Map.Entry<?, ?> e) -> formatSql(e.getValue()))
				.collect(Collectors.joining(","));
		executeQuery(String.format("INSERT INTO %s (%s) VALUES ( %s )", in.getType(), columns, values));
	}

	private boolean update(PersistNode in) {
		String pairs = in.getProperties().entrySet().stream()
				.map((Map.Entry<?, ?> e) -> String.format(" %s = %s", e.getKey(), formatSql(e.getValue())))
				.collect(Collectors.joining(","));
		String id = in.getProperties().entrySet().stream()
				.filter((Map.Entry<String, Object> e) -> "id".equalsIgnoreCase(e.getKey()))
				.findFirst().map((Map.Entry<?, ?> e) -> "id = "+e.getValue()).get();
		return executeQuery(String.format("UPDATE %s SET %s WHERE %s", in.getType(), pairs, id)) > 0;
	}

	private int executeQuery(String sql) {
		Query queryUpdate = em.createNativeQuery(sql);
		return queryUpdate.executeUpdate();
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
