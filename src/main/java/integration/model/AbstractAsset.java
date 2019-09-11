package integration.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import integration.IntegrationUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@Component
@MappedSuperclass
@DynamicInsert
@DynamicUpdate
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractAsset implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final BigInteger B = BigInteger.ONE.shiftLeft(64); // 2^64
	public static final BigInteger L = BigInteger.valueOf(Long.MAX_VALUE);

	@Getter
	@Setter
	@Transient
	private List<Map<String, Object>> relations;

	public static BigInteger convertToBigInteger(UUID id) {
		BigInteger lo = BigInteger.valueOf(id.getLeastSignificantBits());
		BigInteger hi = BigInteger.valueOf(id.getMostSignificantBits());

		// If any of lo/hi parts is negative interpret as unsigned

		if (hi.signum() < 0)
			hi = hi.add(B);

		if (lo.signum() < 0)
			lo = lo.add(B);

		return lo.add(hi.multiply(B));
	}

	public static UUID convertFromBigInteger(BigInteger x) {
		BigInteger[] parts = x.divideAndRemainder(B);
		BigInteger hi = parts[0];
		BigInteger lo = parts[1];

		if (L.compareTo(lo) < 0)
			lo = lo.subtract(B);

		if (L.compareTo(hi) < 0)
			hi = hi.subtract(B);

		return new UUID(hi.longValueExact(), lo.longValueExact());
	}

	@SneakyThrows
	public Integer getId() {
		return (Integer) idField().get(this);
	}

	@SneakyThrows
	public String getSourceId() {
		Optional<EntityType<?>> entityType = entityType(this.getClass());
		Attribute<?, ?> attr = entityType.get().getDeclaredAttribute("sourceId");
		Field javaMember = (Field) attr.getJavaMember();
		return (String) javaMember.get(this);
	}

	@SneakyThrows
	public void setId(Integer intValue) {
		idField().set(this, intValue);
	}

	private Field idField() {
		Optional<EntityType<?>> entityType = entityType(this.getClass());
		SingularAttribute<?, Integer> declaredId = entityType.get().getDeclaredId(Integer.class);
		Field javaMember = (Field) declaredId.getJavaMember();
		return javaMember;
	}

	@Cacheable
	static private Optional<EntityType<?>> entityType(Class<?> clazz) {
		EntityManager em = IntegrationUtils.getBean(EntityManager.class);
//		List<Class<?>> mapped = em.getMetamodel().getEntities().parallelStream().map(EntityType::getJavaType).collect(Collectors.toList());
		return em.getMetamodel().getEntities()
				.parallelStream()
				.filter(et->et.getJavaType().equals(clazz)).findAny();
	}

}