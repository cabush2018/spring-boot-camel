package integration;

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

import spock.lang.Specification

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class AnotherApplicationTest extends Specification {

	@Autowired (required = false)
	private ApplicationContext context

	def "when context is loaded then all expected beans are created"() {
		expect: "the context is created"
		context
	}

	@LocalServerPort
	int port

	def "test POST Map to contextPath expect 2xx"() throws Exception {
		when:
		def restTemplate = new TestRestTemplate()
		def path = "http://localhost:${port}/integration/"
		def data = "{\"integration;model;Concept\":{\"id\": 177, \"name\": \"hello \"}, \"Node\":{\"id\":4,\"name\":\"fnode\",\"active\":false}}"

		then:
		ResponseEntity<?> response = restTemplate
				.withBasicAuth("username", "password")
				.postForEntity(path, data, String.class);
		response.getStatusCode().is2xxSuccessful()

		print response;
	}

	def "test POST Array to contextPath expect 2xx"() throws Exception {
		when:
		def restTemplate = new TestRestTemplate()
		def path = "http://localhost:${port}/integration/all"
		def String data = "[{\"integration;model;Concept\":{\"id\": 177, \"name\": \"hello \"}}, {\"Node\":{\"id\":4,\"name\":\"fnode\",\"since\":\"2019-01-01\",\"active\":true,\"size\":123.456}}]";

		then:
		ResponseEntity<?> response = restTemplate
				.withBasicAuth("username", "password")
				.postForEntity(path, data, String.class);
		response.getStatusCode().is2xxSuccessful()
		print response
	}

	def "test bad passwd POST Array to contextPath expect 4xx"() throws Exception {
		when:
		def restTemplate = new TestRestTemplate()
		def path = "http://localhost:${port}/integration/all"
		def String data = "[{\"integration;model;Concept\":{\"id\": 177, \"name\": \"hello \"}}, {\"Node\":{\"id\":4,\"name\":\"fnode\",\"since\":\"2019-01-01\",\"active\":true,\"size\":123.456}}]";

		then:
		ResponseEntity<?> response = restTemplate
				.withBasicAuth("username", "wrong-password")
				.postForEntity(path, data, String.class);
		response.getStatusCode().is4xxClientError()
		print response
	}

	def "test Post Bogus Object"() throws Exception {
		when:
		def restTemplate = new TestRestTemplate()
		def path = "http://localhost:${port}/integration/all"
		def data = "[1234{\"integration;model;Concept\":{\"id\": 177, \"name\": \"hello \"}}, {\"Node\":{\"id\":4,\"name\":\"fnode\",\"since\":\"2019-01-01\",\"active\":true,\"size\":123.456}}]";
		def appErrorLog = new File("dgp.error.log")
		
		then:
		if (appErrorLog.exists()) {
			appErrorLog.deleteDir()
		}
		! appErrorLog.exists()

		ResponseEntity<?> response = restTemplate
				.withBasicAuth("username", "password")
				.postForEntity(path, data, String.class)

		response.getStatusCode().isError()
		appErrorLog.exists()
		print response
	}
}