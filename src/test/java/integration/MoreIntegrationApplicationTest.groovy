package integration;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
@SpringBootTest
@ContextConfiguration
class LoadContextTest extends Specification {
 
    @Autowired (required = false)
    private ApplicationContext context
 
    def "when context is loaded then all expected beans are created"() {
        expect: "the context is created"
        context
    }
}