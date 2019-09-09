package integration.model;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PrePersist;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Streams;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import integration.IntegrationUtils;
import integration.persistence.PersistenceService;

@Component
public class RelationsListener {

	@PrePersist
	public void persistRelations(AbstractAsset in) {
		List<Map<String, Object>> relations = in.getRelations();
		if (relations!=null) {
			relations.forEach(this::persistRelation);
		}
	}

	ExpressionParser PARSER = new SpelExpressionParser();
	Expression ID = PARSER.parseExpression("['id']");
	Expression SOURCE_ID = PARSER.parseExpression("['source']['id']");
	Expression SOURCE_TYPE = PARSER.parseExpression("['source']['type']");
	Expression TARGET_ID = PARSER.parseExpression("['target']['id']");
	Expression TARGET_TYPE = PARSER.parseExpression("['target']['type']");
	Expression SOURCE_PROPS = PARSER.parseExpression("['source']");
	Expression TARGET_PROPS = PARSER.parseExpression("['target']");

	@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
	private void persistRelation(Map<String, Object> relation) {
		PersistenceService ps = IntegrationUtils.getBean(PersistenceService.class);
		EvaluationContext context = new StandardEvaluationContext(relation);
		Object id = ID.getValue(context);
		Object sourceId = SOURCE_ID.getValue(context);
		String sourceType = (String) SOURCE_TYPE.getValue(context);
		Object targetId = TARGET_ID.getValue(context);
		String targetType = (String) TARGET_TYPE.getValue(context);
		Map sourceProps = (Map) SOURCE_PROPS.getValue(context);
		Map targetProps = (Map) TARGET_PROPS.getValue(context);

		ps.persist(targetType,targetProps);
		
	}


}
