package integration;

import javax.ws.rs.core.MediaType;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import integration.model.Concept;
import integration.persistence.PersistNode;
import integration.persistence.PersistenceService;

@SpringBootApplication
public class Application {

	@Value("${server.port}")
	String serverPort;

	@Value("${integration.api.path}")
	String contextPath;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Bean
	ServletRegistrationBean servletRegistrationBean() {
		ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(),
				contextPath + "/*");
		servlet.setName("CamelServlet");
		return servlet;
	}

	@Component
	class RestApi extends RouteBuilder {

		@Autowired
		PersistenceService persistenceService;

		@Override
		public void configure() {

			@SuppressWarnings("unused")
			CamelContext context = new DefaultCamelContext();

			restConfiguration().contextPath(contextPath) //
					.port(serverPort).enableCORS(true).apiContextPath("/api-doc")
					.apiProperty("api.title", "Integration API").apiProperty("api.version", "v1")
					.apiProperty("cors", "true") // cross-site
					.apiContextRouteId("doc-api").component("servlet").bindingMode(RestBindingMode.json)
					.dataFormatProperty("prettyPrint", "true");

			rest("/api/")
				.id("api-route")
				.post("/direct").type(PersistNode.class)
				//.post("/direct/concept").type(Concept.class)
				.produces(MediaType.APPLICATION_JSON)
				.consumes(MediaType.APPLICATION_JSON)
				.bindingMode(RestBindingMode.auto)
				.enableCORS(true)
				.to("direct:remoteService")
				;

			from("direct:remoteService").routeId("direct-route").tracing().log(">>> ${body.id} - ${body.name}")
					.process(this::process).setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));
		}

		public void process(Exchange exchange) throws Exception {
			Object in = exchange.getIn().getBody();
			persistenceService.persist(in);
			exchange.getIn().setBody(in);
		}

	}
}
