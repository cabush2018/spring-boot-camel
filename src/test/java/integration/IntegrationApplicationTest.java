package integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import integration.persistence.PersistenceService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationApplicationTest {

	@Autowired
	private TestRestTemplate restTemplate;
	
	@Autowired
	private PersistenceService persistenceService;

	@Value("${integration.api.path}")
	private String contextPath;

	@Test
	public void contextLoads() {
		assertNotNull(persistenceService);
	}

	@Test
	public void testPostUnmappedNode() throws Exception {
		// test POST to /requests expect 2xx
		String path="/" + contextPath + "/";
		String data = "{\"integration;model;Concept\":{\"id\": 177, \"name\": \"hello \"}, \"Node\":{\"id\":4,\"name\":\"fnode\"}}";
		System.out.println("\n" + "POST "+path);
	   HttpEntity<?> response = restTemplate.postForEntity(path, data,Map.class);
//	   assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	   System.out.println("Headers:>>> "+response.getHeaders());
	   System.out.println("Body:>>> "+response.getBody());
	}

}