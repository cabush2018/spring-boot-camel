package integration;

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.web.context.WebApplicationContext;

@Configuration
@Profile("dev")
public class Config {

	@Autowired
	private DataSource datasource;

	@Autowired
	private WebApplicationContext context;

	@PostConstruct
	public void process() throws SQLException{
		processFile("classpath:schema-dev.sql");
		processFile("classpath:data-dev.sql");
	}

	private void processFile(String location) throws SQLException {
		Resource resource = context.getResource(location);
		if (resource.exists()) {
			ScriptUtils.executeSqlScript(datasource.getConnection(), resource);
		}
	}
}