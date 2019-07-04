package integration.camel;

import javax.ws.rs.core.MediaType;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Application {

	@Value("${server.port}")
	String serverPort;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	ServletRegistrationBean servletRegistrationBean() {
		ServletRegistrationBean servlet = new ServletRegistrationBean(
				new CamelHttpTransportServlet()
				, "/*");
		servlet.setName("Camel-servlet");
		return servlet;
	}

	@Component
	class RestApi extends RouteBuilder {
		
		@Autowired 
		PersistenceService persistenceService;

		@Override
		public void configure() {

			CamelContext context = new DefaultCamelContext();

			restConfiguration()
					.port(serverPort)
					.enableCORS(true)
					.apiContextPath("/api-doc")
					.apiProperty("api.title", "Test REST API")
					.apiProperty("api.version", "v1")
					.apiProperty("cors", "true") // cross-site
					.apiContextRouteId("doc-api")
					.component("servlet")
					.bindingMode(RestBindingMode.json)
					.dataFormatProperty("prettyPrint", "true");
			/**
			 * The Rest DSL supports automatic binding json/xml contents to/from POJOs using
			 * Camels Data Format. By default the binding mode is off, meaning there is no
			 * automatic binding happening for incoming and outgoing messages. You may want
			 * to use binding if you develop POJOs that maps to your REST services request
			 * and response types.
			 */

			rest("/api/")
					.id("api-route")
					.post("/integration")
					.produces(MediaType.APPLICATION_JSON)
					.consumes(MediaType.APPLICATION_JSON)
//                .get("/hello/{place}")
					.bindingMode(RestBindingMode.auto)
					.type(DirectProcessor.class)
					.enableCORS(true)
//                .outType(OutBean.class)
					.to("direct:remoteService");

			from("direct:remoteService")
				.routeId("direct-route")
				.tracing()
				.log(">>> ${body.id} - ${body.name}")
//                .transform(basePackages = "integration")().simple("blue ${in.body.name}")                
					.process(new Processor() {
						@Override
						public void process(Exchange exchange) throws Exception {
							DirectProcessor bodyIn = (DirectProcessor) exchange.getIn().getBody();

							persistenceService.example(bodyIn);

							exchange.getIn().setBody(bodyIn);
						}
					}).setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));
		}
	}
}
