package integration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spi.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/*test with one of
curl --header 'Content-Type: application/json' --request POST --data '{"id": 1,"name": "hello "}' http://localhost:9080/integration/Concept
curl --header 'Content-Type: application/json' --request POST --data '{"integration;model;Concept":{"id": 177, "name": "hello "}, "Node":{"id":4,"name":"fnode"}}' http://localhost:9080/integration/
curl --header 'Content-Type: application/json' --request POST --data '[{"integration;model;Concept":{"id": 177, "name": "hello "}}, {"Node":{"id":4,"name":"fnode"}}]' http://localhost:9080/integration/all

ab -n 1 -c 1 -T 'Content-Type: application/json' -p ./bin/post.array.txt http://localhost:9080/integration/all

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

	int sizePool = Runtime.getRuntime().availableProcessors();

	private static final Logger logger = LoggerFactory.getLogger(IntegrationApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(IntegrationApplication.class, args);
	}

	@Bean(name = "servletRegistrationBeanCamel")
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
				Throwable rootEx = this.getRootCause(exception);
				String infoStr = Arrays.asList(exchange.getIn().getBody(), exception, rootEx, rootEx.getMessage())
						.stream().map(Object::toString).collect(Collectors.joining("\n"));
				Message msg = exchange.getOut();
				msg.setBody(infoStr, String.class);
				logger.error("error response", rootEx);
			}

			private Throwable getRootCause(Throwable e) {
				while (e.getCause() != null && !e.equals(e.getCause())) {
					e = e.getCause();
				}
				return e;
			}
		}
				
		@Value("${app.processing.entry:seda:input}")
		String entryProcessing;

		@SuppressWarnings("deprecation")
		@Override
		public void configure() {

			errorHandler(defaultErrorHandler().maximumRedeliveries(0));

			onException(org.apache.camel.CamelAuthorizationException.class).handled(true)
					.transform(simple("Access Denied with the Policy of ${exception.policyId} !"))
					.setHeader(Exchange.HTTP_RESPONSE_CODE, simple("401"));
			
			onException(Exception.class).handled(true).maximumRedeliveries(0).transform()
					.simple("${date:now:yyyy-MM-dd HH:mm:ssZ} -- ${body}").bean(PrepareErrorResponse.class).multicast()
					.to("{{app.error.log}}").to("log:integration.LOG?level=ERROR")
					.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
					.setFaultHeader(Exchange.HTTP_RESPONSE_CODE, constant(400)).end();

			
			restConfiguration()
				.component("servlet")
				.contextPath(contextPath).port(serverPort).enableCORS(true).apiContextPath("/api-doc")
				.apiProperty("api.title", "Integration API").apiProperty("api.version", "v1")
				.apiProperty("cors", "true").apiContextRouteId("doc-api").component("servlet")
				//.endpointProperty("handlers", "securityHandler")
				.bindingMode(RestBindingMode.json).dataFormatProperty("prettyPrint", "true");

			final String STAGED_INPUT = "seda:input";
			final String REMOTE_PERSISTENCE = "direct:remote-persistence";
			final String LOCAL_PERSISTENCE = "direct:local-persistence";
			final String PERSISTENCE = "bean:persistenceService?method=persist(${body})";
			
//			Policy policy = new AuthorizationPolicy() ;
			
			rest("/").produces(MediaType.APPLICATION_JSON).consumes(MediaType.APPLICATION_JSON).enableCORS(true)
					.post("/")
						.description("POST an entity whose mapping state is unknown, with the intent to be persisted.")
						.route().routeId("direct").inputType(Map.class).outputType(Map.class)
//						.policy(policy)
//						.filter().simple("${body} contains 'Node'")
//							.log(LoggingLevel.TRACE, "payload ${body}")
//							.end()
						.to(entryProcessing)
						.endRest()
					.post("/all")
						.description(
								"POST an array of entites whose mapping states is unknown, with the intent to be all persisted.")
						.route().routeId("direct-array").inputType(List.class)
						.split(body())
						.to(entryProcessing)
						.endRest()
					.post("/{type}")
						.description(
								"POST an entity of dynamic {type}, to be persistCalendar.getInstance().getTime().toGMTString()+\"$\"ed according to its JPA mappings.")
						.route().routeId("direct-mapped").inputType(Map.class)
						.to("bean:integrationConverter?method=toPersistent(${header.type},${body})")
						.to(entryProcessing);

			from(STAGED_INPUT).threads(sizePool).maxQueueSize(sizeQueue).to(REMOTE_PERSISTENCE);

			from(REMOTE_PERSISTENCE).hystrix().to(LOCAL_PERSISTENCE).onFallback().transform()
					.simple("FALLBACK Hystrix ${body}").log("${body}").end();

			from(LOCAL_PERSISTENCE).to(PERSISTENCE).end();
		}

	}
	
	@Component
	public class NettyWebServerFactoryPortCustomizer 
	  implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
	 
	    @Override
	    public void customize(NettyReactiveWebServerFactory serverFactory) {
	        serverFactory.setPort(Integer.parseInt(serverPort));
	    }
	}
}
