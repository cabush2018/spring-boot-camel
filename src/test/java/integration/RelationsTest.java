package integration;

import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableMap;

import integration.entity.Asset;
import integration.entity.Element;
import integration.entity.Employee;
import integration.entity.Part;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Configuration
@ComponentScan("integration")
@EnableSpringConfigured
@EnableAspectJAutoProxy
@DirtiesContext
public class RelationsTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Value("${integration.api.path}")
	private String contextPath;

	Class<?> clazz = Object[].class;

	@Test
	public void testRelations() {
		final String path = "/" + contextPath + "/all";

		Element e1 = Element.builder().idelement(1).name("element_1").build();
		Part p1 =Part.builder().idpart(1).name("part_1").build();
		Object[] data = { 
			ImmutableMap.of(
//				String.format("%s;%s", p1.getClass().getPackage().getName(), p1.getClass().getSimpleName()), p1, 
				String.format("%s;%s", e1.getClass().getPackage().getName(), e1.getClass().getSimpleName()),
					ImmutableMap.of(
						"idelement", 1,
						"name", "element_1",
						"relations",new Object[]{
								ImmutableMap.of(						
									"id",123,
									"target", ImmutableMap.of(
												"id",1,
												"type","integration.entity;Part"),
									"source",ImmutableMap.of(
											"id",1,
											"type","integration.entity;Part")
								)
						  }
					)
			) 
		};
		ResponseEntity<?> response = restTemplate.withBasicAuth("username", "password").postForEntity(path, data,
				clazz);
		assertTrue(response.getStatusCode().is2xxSuccessful());
	}
}

@Repository
interface AssetRepository extends JpaRepository<Asset, String> {
	public Optional<Asset> findByIdasset(int id);
}

@Repository
interface ElementRepository extends JpaRepository<Element, String> {
	public Optional<Element> findByIdelement(int id);
}

@Repository
interface EmployeeRepository extends JpaRepository<Employee, String> {
	public Optional<Employee> findByIdemployee(int id);
}

@Repository
interface PartRepository extends JpaRepository<Part, String> {
	public Optional<Part> findByIdpart(int id);
}
