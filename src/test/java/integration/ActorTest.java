package integration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import integration.model.Actor;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Configuration
@ComponentScan("integration")
@EnableSpringConfigured
@EnableAspectJAutoProxy
@DirtiesContext
public class ActorTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Value("${integration.api.path}")
	private String contextPath;

	@Autowired
	ActorRepository actorRepository;

	Class<?> clazz = Object[].class;

	@Test
	public void testActorNoId() {
		String uuid = UUID.randomUUID().toString();
		Actor object = new Actor(uuid, null,"charlie chaplin");
		String path = "/" + contextPath + "/all";
		Object[] data = { ImmutableMap.of(
				String.format("%s;%s", object.getClass().getPackage().getName(), object.getClass().getSimpleName()),
				object) };

		assertThat(actorRepository.findBySourceId(uuid).isPresent(), is(false));

		ResponseEntity<?> response = restTemplate.withBasicAuth("username", "password").postForEntity(path, data,
				clazz);
		assertTrue(response.getStatusCode().is2xxSuccessful());
		assertThat(actorRepository.findBySourceId(uuid).isPresent(), is(true));
	}

	@Test
	public void testActorHavingId() {
		String uuid = UUID.randomUUID().toString();
		int id = (int) (Integer.MAX_VALUE * Math.random());
		Actor object = new Actor(uuid, id,"groucho marx");
		String path = "/" + contextPath + "/all";
		Object[] data = { ImmutableMap.of(
				String.format("%s;%s", object.getClass().getPackage().getName(), object.getClass().getSimpleName()),
				object) };

		assertThat(actorRepository.findById(id).isPresent(), is(false));

		ResponseEntity<?> response = restTemplate.withBasicAuth("username", "password").postForEntity(path, data,
				clazz);

		assertTrue(response.getStatusCode().is2xxSuccessful());
		assertThat(actorRepository.findById(id).isPresent(), is(true));
	}
}

@Repository
interface ActorRepository extends JpaRepository<Actor, String> {
	public Optional<Actor> findBySourceId(String sourceId);

	public Optional<Actor> findById(int id);
}
