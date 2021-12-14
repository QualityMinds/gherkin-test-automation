package de.qualityminds.gta.driver.db2;

import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DB2Driver {
	private static final Logger logger = LoggerFactory.getLogger(DB2Driver.class);
	private static Connection connection = null;

	private static void initConnection(ConnectionSettings settings) throws SQLException, ClassNotFoundException {
		logger.info("Initiating new db2 connection with {}", settings);
		String db2Url = String.format("jdbc:db2://%s:%s/%s", settings.getHost(), settings.getPort(),
				settings.getDatabase());
		String db2User = settings.getUser();
		String db2Password = settings.getPassword();
		String db2Schema = settings.getSchema();

		long startRetrievingConnection = System.currentTimeMillis();
		Security.insertProviderAt(new BouncyCastleProvider(), 1);


		Properties properties = new Properties();
		properties.put("user", db2User);
		properties.put("password", db2Password);
		if(settings.isUseSSL()){
			properties.put("sslConnection", "true");
		}

		DriverManager.setLoginTimeout(60);
		connection = DriverManager.getConnection(db2Url, properties);
		long connectionAvailable = System.currentTimeMillis();
		logger.info("Connection retrieved from DriverManager - took {}s", ((connectionAvailable - startRetrievingConnection) / 1000));
		connection.setSchema(db2Schema);
	}

	public static Connection getConnection(ConnectionSettings settings) throws SQLException {
		try {
			if (connection == null || connection.isClosed()) {
				initConnection(settings);
				return connection;
			}
			if (!connection.getSchema().equals(settings.getSchema())) {
				connection.close();
				initConnection(settings);
			}
			return connection;
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("{}\n{}", e.getMessage(), Arrays.toString(e.getStackTrace()));
			}
		}
		super.finalize();
	}

}
