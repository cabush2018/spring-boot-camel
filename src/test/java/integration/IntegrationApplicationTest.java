package integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import integration.model.Concept;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class IntegrationApplicationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test @Ignore
	public void sayHelloTest() {
		// Call the REST API
		ResponseEntity<Object> response = restTemplate.postForEntity("/integration/Concept",
				Concept.builder().name("first concept").build(), Object.class);
		Object body = response.getBody();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}