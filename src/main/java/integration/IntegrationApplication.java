package integration;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/*test with one of
curl --header 'Content-Type: application/json' --request POST --data '{"id": 1,"name": "hello "}' http://localhost:9080/integration/Concept
curl --header 'Content-Type: application/json' --request POST --data '{"integration;model;Concept":{"id": 177, "name": "hello "}, "Node":{"id":4,"name":"fnode"}}' http://localhost:9080/integration/
curl --header 'Content-Type: application/json' --request POST --data '[{"integration;model;Concept":{"id": 177, "name": "hello "}}, {"Node":{"id":4,"name":"fnode"}}]' http://localhost:9080/integration/all
*/
@SpringBootApplication
@EnableConfigurationProperties
@EntityScan(basePackages = { "integration.model" })
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
		public RestApi(CamelContext context) {
		}

		@Override
		public void configure() {
            onException(Exception.class)
	            .handled(true)
	            .maximumRedeliveries(1)
	            .logStackTrace(false)
	            .logExhausted(false)
	            .log(LoggingLevel.ERROR, "Failed processing ${body}")
	            .process(this::date)
	            //.enrich("timer:time")
	            .transform(body().prepend(Calendar.getInstance().getTime().toGMTString()+"$"))
	            .to("file:{{app.error.log}}")
	            ;
            
			restConfiguration().contextPath(contextPath).port(serverPort).enableCORS(true).apiContextPath("/api-doc")
					.apiProperty("api.title", "Integration API").apiProperty("api.version", "v1")
					.apiProperty("cors", "true").apiContextRouteId("doc-api").component("servlet")
					.bindingMode(RestBindingMode.json).dataFormatProperty("prettyPrint", "true");

			rest("/").produces(MediaType.APPLICATION_JSON).consumes(MediaType.APPLICATION_JSON).enableCORS(true)
					.post("/")
					.description("POST an entity whose mapping state is unknown, with the intent to be persisted.")
					.route().routeId("direct").inputType(Map.class).log("${body}")
					.to("bean:persistenceService?method=persist(${body})").endRest().post("/all")
					.description(
							"POST an array of entites whose mapping states is unknown, with the intent to be all persisted.")
					.route().routeId("direct-array").inputType(List.class).log("${body}")
					.to("bean:persistenceService?method=persist(${body})").endRest().post("/{type}")
					.description("POST an entity of dynamic {type}, to be persistCalendar.getInstance().getTime().toGMTString()+\"$\"ed according to its JPA mappings.")
					.route().routeId("direct-mapped").inputType(Map.class).log("${header.type} -- ${body}")
					.to("bean:persistenceService?method=persist(${header.type}, ${body})");
			
		}

//		from("direct:remoteService").routeId("direct-route").tracing().log(">>> ${body.id} - ${body.name}")
//		.to("seda:input");
//
//from("seda:input").threads(5).to("bean:persistenceService?method=persist(${body})")
//	.to("bean:createResponse")
//	.process(this::process).setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpStatus.ACCEPTED))
;

// java
//from("mina:tcp://0.0.0.0:9876?textline=true&sync=true") 
//	
//	.to("seda:input"); 
//from("seda:input") 
//	.threads(5)
//	.to("bean:processInput") 
//	.to("bean:createResponse"); 
//
//from("direct:remoteService").routeId("direct-route").tracing().log(">>> ${body.id} - ${body.name}")
//		.process(this::process).setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpStatus.ACCEPTED));

		public void date(Exchange exchange) throws Exception {
			Object in = exchange.getIn().getBody();
			exchange.getIn().setBody(Calendar.getInstance().getTime().toGMTString()+"$"+in);
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
