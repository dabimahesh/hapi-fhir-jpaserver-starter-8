package ca.uhn.fhir.jpa.starter.custom.dbaccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceProvider {

	private static DataSource dataSource;

	@Autowired
	public DataSourceProvider(DataSource ds) {
		dataSource = ds;
	}

	public static DataSource getDataSource() {
		return dataSource;
	}
}