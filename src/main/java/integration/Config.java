package integration;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.EntityType;
import javax.sql.DataSource;

import org.hibernate.metamodel.internal.EntityTypeImpl;
import org.hibernate.metamodel.internal.SingularAttributeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.web.context.WebApplicationContext;

import integration.model.Concept;
import lombok.SneakyThrows;

@Configuration
@Profile("dev")
public class Config {

	@Autowired
	private DataSource datasource;

	@Autowired
	private WebApplicationContext context;

	@PostConstruct
	public void process() throws SQLException {
		processFile("classpath:schema-dev.sql");
		processFile("classpath:data-dev.sql");
	}

	private void processFile(String location) throws SQLException {
		Resource resource = context.getResource(location);
		if (resource.exists()) {
			ScriptUtils.executeSqlScript(datasource.getConnection(), resource);
		}
	}

	@FunctionalInterface
	public interface IdExtractor {
		Object extractId(Object obj);
	}

	@Bean
	@ConditionalOnClass(EntityTypeImpl.class)
	public IdExtractor hibernateIdExtractor(EntityManager em) {
		return new IdExtractor() {

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			@SneakyThrows
			public Object extractId(Object obj) {
				Optional<EntityType<?>> eto = em.getMetamodel().getEntities().parallelStream()
						.filter((EntityType et) -> {
							return et.getJavaType().equals(obj.getClass());
						}).findFirst();
				if (!eto.isPresent()) {
					throw new PersistenceException();
				}
				EntityTypeImpl eti = (EntityTypeImpl) eto.get();
				Class clazz = eti.getIdType().getJavaType();
				SingularAttributeImpl idImpl = (SingularAttributeImpl) eti.getId(clazz);
				Field javaMember = (Field) idImpl.getJavaMember();
				return javaMember.get(obj);
			}
		};
	}
}