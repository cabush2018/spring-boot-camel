package integration;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/*test with one of
curl --header 'Content-Type: application/json' --request POST --data '{"id": 1,"name": "hello "}' http://localhost:9080/integration/Concept
curl --header 'Content-Type: application/json' --request POST --data '{"integration;model;Concept":{"id": 177, "name": "hello "}, "Node":{"id":4,"name":"fnode"}}' http://localhost:9080/integration/
curl --header 'Content-Type: application/json' --request POST --data '[{"integration;model;Concept":{"id": 177, "name": "hello "}}, {"Node":{"id":4,"name":"fnode"}}]' http://localhost:9080/integration/all
*/
@SpringBootApplication
@EnableConfigurationProperties
@EnableCaching
@EntityScan(basePackages = { "integration.model" })
public class IntegrationApplication {
	@Value("${server.port}")
	String serverPort;

	@Value("${integration.api.path}")
	String contextPath;
	
	@Value("${integration.queue:1000}")
	int sizeQueue;
	
	int sizePool=Runtime.getRuntime().availableProcessors();

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
		
		public class PrepareErrorResponse {
			@Handler
		    public void prepareErrorResponse(Exchange exchange) {
				Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		        Message msg = exchange.getOut();
		        msg.setFault(false);
		    }
		}
		
		IntegrationConverter converter;
		public RestApi(CamelContext context, IntegrationConverter converter) {
			this.converter=converter;
		}

		@Override
		public void configure() {
            onException(Exception.class)
	            .handled(true)
	            .maximumRedeliveries(0)
//	            .logStackTrace(true)
//	            .logExhausted(true)
	            .log(LoggingLevel.ERROR, "Failed processing ${body}")	            
	            .transform().simple("${date:now:yyyy-MM-dd HH:mm:ss}$ ${body}")
	            //.transform(body().prepend(Calendar.getInstance().getTime().toGMTString()+"$"))
//            	.bean(PrepareErrorResponse.class)
	            .to("{{app.error.log}}")
	            .end();
            
			restConfiguration().contextPath(contextPath).port(serverPort).enableCORS(true).apiContextPath("/api-doc")
				.apiProperty("api.title", "Integration API").apiProperty("api.version", "v1")
				.apiProperty("cors", "true").apiContextRouteId("doc-api").component("servlet")
				.bindingMode(RestBindingMode.json).dataFormatProperty("prettyPrint", "true");

			rest("/").produces(MediaType.APPLICATION_JSON).consumes(MediaType.APPLICATION_JSON).enableCORS(true)
				.post("/")
					.description("POST an entity whose mapping state is unknown, with the intent to be persisted.")
					.route().routeId("direct")
					.inputType(Map.class)
					.to("seda:input").endRest()
				.post("/all")
					.description("POST an array of entites whose mapping states is unknown, with the intent to be all persisted.")
					.route().routeId("direct-array")
					.inputType(List.class)
					.to("seda:input").endRest()
				.post("/{type}")
					.description("POST an entity of dynamic {type}, to be persistCalendar.getInstance().getTime().toGMTString()+\"$\"ed according to its JPA mappings.")
					.route().routeId("direct-mapped").inputType(Map.class)
					.to("bean:integrationConverter?method=toPersistent(${header.type},${body})")
					.to("seda:input");
			
			from("seda:input")
				.threads(sizePool)
				.maxQueueSize(sizeQueue)
				.log("${body}")
				.to("bean:persistenceService?method=persist(${body})");
		}

	}
}
