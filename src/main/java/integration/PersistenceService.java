package integration;

import java.sql.Date;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
public class PersistenceService {

	@PersistenceContext
	EntityManager em;

	@Transactional
	public void persist(PersistNode in) {
		if (update(in)) {
			return;
		}
		insert(in);
	}

	private void insert(PersistNode in) {
		String columns = in.getProperties().keySet().stream().collect(Collectors.joining(","));
		String values = in.getProperties().entrySet().stream().map((Map.Entry<?, ?> e) -> formatSql(e.getValue()))
				.collect(Collectors.joining(","));
		String sqlInsert = String.format("INSERT INTO %s (%s) VALUES ( %s )", in.getType(), columns, values);
		Query queryInsert = em.createNativeQuery(sqlInsert);
		queryInsert.executeUpdate();
	}

	private boolean update(PersistNode in) {
		String pairs = in.getProperties().entrySet().stream()
				.map((Map.Entry<?, ?> e) -> String.format(" %s = %s", e.getKey(), formatSql(e.getValue())))
				.collect(Collectors.joining(","));
		String sqlUpdate = String.format("UPDATE %s SET %s", in.getType(), pairs);
		Query queryUpdate = em.createNativeQuery(sqlUpdate);
		boolean updated = queryUpdate.executeUpdate() > 0;
		return updated;
	}

	private String formatSql(Object e) {
		if (e == null) {
			return "null";
		}
		String value=e.toString();
		if (e instanceof Date || e instanceof String) {
			return String.format("'%s'", value);
		} else if(e instanceof Number || e instanceof Boolean) {
			return value;
		} else {
			throw new RuntimeException("unexpected type");
		}
	}
}
