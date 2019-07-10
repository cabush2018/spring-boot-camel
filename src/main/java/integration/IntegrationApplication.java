package integration;

import javax.ws.rs.core.MediaType;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import integration.model.Thing;
import integration.persistence.PersistNode;
import integration.persistence.PersistenceService;

@SpringBootApplication
public class IntegrationApplication {

	@Value("${server.port}")
	String serverPort;

	@Value("${integration.api.path}")
	String contextPath;

	public static void main(String[] args) {
		SpringApplication.run(IntegrationApplication.class, args);
	}

	@Bean
	ServletRegistrationBean<CamelHttpTransportServlet> servletRegistrationBean() {
		ServletRegistrationBean<CamelHttpTransportServlet> servlet = new ServletRegistrationBean<>(
				new CamelHttpTransportServlet(), contextPath + "/*");
		servlet.setName("CamelServlet");
		return servlet;
	}

	@Component
	class RestApi extends RouteBuilder {
		private PersistenceService persistenceService;
		@SuppressWarnings("unused")
		private CamelContext context;

		public RestApi(CamelContext context, PersistenceService persistenceService) {
			this.context = context;
			this.persistenceService = persistenceService;
		}

		@Override
		public void configure() {
			restConfiguration().contextPath(contextPath) 
					.port(serverPort).enableCORS(true)//.apiContextPath("/api-doc")
					.apiProperty("api.title", "Integration API").apiProperty("api.version", "v1")
					.apiProperty("cors", "true") 
					.apiContextRouteId("doc-api").component("servlet")
					.bindingMode(RestBindingMode.json)
					.dataFormatProperty("prettyPrint", "true")
					;

	          rest("/")
		          .produces(MediaType.APPLICATION_JSON).consumes(MediaType.APPLICATION_JSON)
		          .post("/direct")
		          		.enableCORS(true)
		          		.route().routeId("direct")
		          		.inputType(PersistNode.class)
		                .log("${body}")
						.to("direct:remoteService")
//						.transform().constant("Hello World!")
						.endRest()
		           .post("/direct/thing")
		           		.enableCORS(true)
		           		.route().routeId("thing")
//		           		.bean("hello")
		           		.inputType(Thing.class)
		           		.log("${body}")
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
		
//https://dzone.com/articles/apache-camel-integration
//public class OrderRouter extends RouteBuilder {
//
//    @Override
//    public void configure() throws Exception {
//        JaxbDataFormat jaxb = new JaxbDataFormat("org.fusesource.camel");
//        
//        // Receive orders from two endpoints
//        from("file:src/data?noop=true").to("jms:incomingOrderQueue");
//        from("jetty:http://localhost:8888/placeorder")
//          .inOnly().to("jms:incomingOrderQueue")
//          .transform().constant("OK");
//
//        // Do the normalization
//        from("jms:incomingOrderQueue")
//         .convertBodyTo(String.class)
//         .choice()
//           .when().method("orderHelper", "isXml")
//             .unmarshal(jaxb)
//             .to("jms:orderQueue")
//           .when().method("orderHelper", "isCsv")
//             .unmarshal().csv()         
//             .to("bean:normalizer")
//             .to("jms:orderQueue");
//    }
//
//}		

	}
}
