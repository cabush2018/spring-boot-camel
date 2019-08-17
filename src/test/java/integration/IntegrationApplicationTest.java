package integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;

import org.hamcrest.core.IsNot;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import integration.model.Actor;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class IntegrationApplicationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Value("${integration.api.path}")
	private String contextPath;

	@Test
	public void testPostMapOfMappedJPAAndUnmappedNode() throws Exception {
		// test POST Map to contextPath expect 2xx
		String path = "/" + contextPath + "/";
		String data = "{\"integration;model;Concept\":{\"id\": 177, \"name\": \"hello \"}, \"Node\":{\"id\":4,\"name\":\"fnode\",\"active\":false}}";
		ResponseEntity<?> response = restTemplate.withBasicAuth("username", "password").postForEntity(path, data,
				String.class);
		assertTrue(response.getStatusCode().is2xxSuccessful());
		System.out.println(response);
	}

	@Test
	public void testPostArrayOfMappedJPAAndUnmappedNode() throws Exception {
		// test POST Array to contextPath expect 2xx
		String path = "/" + contextPath + "/all";
		String data = "[{\"integration;model;Concept\":{\"id\": 177, \"name\": \"hello \"}}, {\"Node\":{\"id\":4,\"name\":\"fnode\",\"since\":\"2019-01-01\",\"active\":true,\"size\":123.456}}]";
		ResponseEntity<?> response = restTemplate.withBasicAuth("username", "password").postForEntity(path, data,
				String.class);
		assertTrue(response.getStatusCode().is2xxSuccessful());
		System.out.println(response);
	}

	@Test
	public void testPostArrayOfMappedJPAAndUnmappedNodeFailAuthn() throws Exception {
		// test POST Array to contextPath expect 2xx
		String path = "/" + contextPath + "/all";
		String data = "[{\"integration;model;Concept\":{\"id\": 177, \"name\": \"hello \"}}, {\"Node\":{\"id\":4,\"name\":\"fnode\",\"since\":\"2019-01-01\",\"active\":true,\"size\":123.456}}]";
		ResponseEntity<?> response = restTemplate.withBasicAuth("username", "wrong-password").postForEntity(path, data,
				String.class);
		assertTrue(response.getStatusCode().is4xxClientError());
		System.out.println(response);
	}

	@Test
	public void testActorNoId() {
		Actor actor = Actor.builder().name("charlie chaplin").sourceId(UUID.randomUUID().toString()).build();
		actor.init();
		assertThat(actor.getId(), notNullValue());

		String path = "/" + contextPath + "/all";
		Actor[] data = {actor};
		ResponseEntity<?> response = restTemplate.withBasicAuth("username", "password")
				.postForEntity(path, data, String.class);
		assertTrue(response.getStatusCode().is2xxSuccessful());
		System.out.println(response);

	}

	@Test
	public void testPostBogusObject() throws Exception {
		String logLocation = "dgp.error.log";
		Path appErrorLog = Paths.get(logLocation);
		if (appErrorLog.toFile().exists()) {
			Files.walk(appErrorLog).sorted(Comparator.reverseOrder()).map(Path::toFile).peek(System.out::println)
					.forEach(File::delete);
			appErrorLog.toFile().delete();
		}
		assertFalse(appErrorLog.toFile().exists());

		// test POST Array to contextPath expect 2xx
		String path = "/" + contextPath + "/all";
		String data = "[1234{\"integration;model;Concept\":{\"id\": 177, \"name\": \"hello \"}}, {\"Node\":{\"id\":4,\"name\":\"fnode\",\"since\":\"2019-01-01\",\"active\":true,\"size\":123.456}}]";
		ResponseEntity<?> response = restTemplate.withBasicAuth("username", "password").postForEntity(path, data,
				String.class);
		assertTrue(response.getStatusCode().isError());
		assertTrue(appErrorLog.toFile().exists());
		System.out.println(response);
	}
}