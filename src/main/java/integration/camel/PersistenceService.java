package integration.camel;

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
		String columns = in.properties.keySet().stream().collect(Collectors.joining(","));
		String values = in.properties.entrySet().stream().map(e -> e.getValue().toString()).collect(Collectors.joining(","));
		String sqlInsert = String.format("INSERT INTO %s (%s) VALUES ( %s )", in.type, columns, values);
		Query queryInsert = em.createNativeQuery(sqlInsert);
		queryInsert.executeUpdate();
	}

	private boolean update(PersistNode in) {
		String pairs = in.properties.entrySet().stream()
				.map((Map.Entry<?, ?> e) -> String.format(" %s = %s", e.getKey(), e.getValue()))
				.collect(Collectors.joining(","));
		String sqlUpdate = String.format("UPDATE %s SET %s", in.type, pairs);
		Query queryUpdate = em.createNativeQuery(sqlUpdate);
		boolean updated = queryUpdate.executeUpdate() > 0;
		return updated;
	}
}
