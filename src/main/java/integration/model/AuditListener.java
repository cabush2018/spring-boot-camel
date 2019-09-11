package integration.model;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.IntegrationUtils;
import integration.persistence.PersistNode;
import integration.persistence.PersistenceService;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class AuditListener {

	private static final Logger logger = LoggerFactory.getLogger(AuditListener.class);

	@Getter
	@AllArgsConstructor
	enum Operation {
		create("CREATE"), update("UPDATE"), delete("DELETE"),;

		private String value;
	}

	@PrePersist
	public void auditPersist(AbstractAsset asset) {
		performAudit(asset, Operation.update);
	}

	@PreRemove
	public void auditRemove(AbstractAsset asset) {
		performAudit(asset, Operation.delete);
	}

	@Transactional(Transactional.TxType.MANDATORY)
	private void performAudit(AbstractAsset asset, Operation op) {
		Class<? extends AbstractAsset> clazz = asset.getClass();
		Class<?> clazzAudit = null;

		try {
			clazzAudit = Class.forName(clazz.getCanonicalName() + "Audit");
		} catch (ClassNotFoundException e) {
			logger.error("No audit class:", e);
		}
		try {
			EntityManager em = IntegrationUtils.getBean(EntityManager.class);
			Query query = em.createQuery(String.format("select sourceId from %s c where sourceId = '%s'",
					clazz.getSimpleName(), asset.getSourceId()));
			List<?> results = query.getResultList();
			if (op != Operation.delete && results.isEmpty())
				op = Operation.create;

			Map<String, String> props = BeanUtils.describe(asset);
			props.put("operation", op.getValue());
			props.put("createdOn", Calendar.getInstance().getTime().toString());
			props.put("createdBy", "bridge");

			PersistenceService persistence = IntegrationUtils.getBean(PersistenceService.class);
			persistence.persist(PersistNode.builder().type(clazz.getSimpleName().toLowerCase() + "_audit").properties(props).build());


		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			logger.error("Error while preparing audit:", e.getMessage());
		}
	}

}
