package integration.persistence;

import java.sql.Date;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
public class PersistenceService {

	@PersistenceContext
	EntityManager em;

	@Transactional
	public void persist(Object in) {
		if (isMapped(in)) {
			persistMapped(in);
		} else if (in instanceof PersistNode) {
			persistUnmapped((PersistNode) in);
		} else {
			throw new TypeNotPresentException(in.getClass().getCanonicalName(), null);
		}
	}

	private boolean isMapped(Object in) {
		boolean isMapped = em. getMetamodel().getEntities().parallelStream()
				.map(EntityType::getJavaType)
				.anyMatch(in.getClass()::equals);
		return isMapped;
	}

	private void persistUnmapped(PersistNode in) {
		if (update(in)) {
			return;
		}
		insert(in);
	}

	private void persistMapped(Object in) {
		em.persist(in);
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
		return executeQuery(String.format("UPDATE %s SET %s", in.getType(), pairs))>0;
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
