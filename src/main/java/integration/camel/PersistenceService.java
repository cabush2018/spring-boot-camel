package integration.camel;

import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Service;

/**
 * Hook in here the persistence layer inside Camel
 */
@Service
public class PersistenceService {
	@PersistenceContext
	PersistenceContext persistenceContext;
	public void example(DirectProcessor bodyIn) {
	//	persistenceContext.
		bodyIn.setName("Hello there..., " + bodyIn.getName());
		bodyIn.setId(bodyIn.getId() * 10);
	}
}
