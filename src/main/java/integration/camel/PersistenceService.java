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
	public void example(PersistNode bodyIn) {
	//	persistenceContext.
		bodyIn.prop("name","Hello there..., " + bodyIn.getName());
		bodyIn.prop("id", bodyIn.getId() * 10);
	}
}
