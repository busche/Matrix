package de.ismll.database.mssql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.utilities.Assert;

/**
 * System properties
 * 
 * 
 * 
 * @author John
 * 
 */
public class MsSQL {

	private Logger logger = LogManager.getLogger(getClass());

	private DataSource dataSource;

	private boolean connected;

	private Properties prop = new Properties();

	private String databaseName;

	public String getDatabaseName() {
		return databaseName;
	}

	public MsSQL() throws IOException {
		super();
		init();
	}

	private File findFile(String name) {
		return findFile(name, false);
	}

	private File findFile(String name, boolean terminate) {
		return findFile(name, logger, terminate);
	}

	public static File findFile(String name, Logger logger, boolean terminate) {
		File ret = null;
		File prop_file0 = new File(name);
		File prop_file_eclipse = new File("scripts/" + name);
		File prop_file1 = new File("src/java/" + name);
		File prop_file2 = new File(System.getProperty("user.home") + "/."
				+ name);
		String property = System
				.getProperty("de.ismll.database.mssql." + name);
		File prop_file3 = null;
		if (property != null) {
			prop_file3 = new File(property);
		}
		if (prop_file0.isFile())
			ret = prop_file0;
		if (prop_file_eclipse.isFile())
			ret = prop_file_eclipse;
		if (prop_file1.isFile())
			ret = prop_file1;
		if (prop_file2.isFile())
			ret = prop_file2;
		if (prop_file3 != null && !prop_file3.isFile()) {
			logger.warn("DB-Configuration was specified through a System property, but could not be found directly. Searching in lookup-paths ...");
			File findFile = findFile(property, logger, false);
			if (findFile != null)
				prop_file3 = findFile;
		}
		if (prop_file3 != null && prop_file3.isFile())
			ret = prop_file3;

		if (ret == null) {
			logger.fatal("File " + name
					+ " was not found at the following locations:");
			logger.fatal("\t" + prop_file0);
			logger.fatal("\t" + prop_file_eclipse);
			logger.fatal("\t" + prop_file1);
			logger.fatal("\t" + prop_file2);
			if (prop_file3 != null)
				logger.fatal("\t" + prop_file3);
			if (terminate) {
				System.exit(1);
				throw new RuntimeException(
						"No "
								+ name
								+ " file found. You may want to define -Dde.ismll.database.mssql."
								+ name + " system property.");
			}
		}
		logger.debug("Using " + name + " from " + ret);
		return ret;
	}

	private synchronized void init() {
		File db_properties = findFile("db.properties", true);

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(db_properties);
		} catch (FileNotFoundException e1) {
			// should not happen
			throw new RuntimeException(e1);
		}
		try {
			prop.load(fis);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				logger.debug(e.getMessage(), e);
			}
		}
		databaseName = prop.getProperty("MsSql.dbname", null);
		Assert.notNull(databaseName, db_properties + "[MsSql.dbname]");

	}

	public void connect() {

		if (!connected) {
			dataSource = setupDataSource(prop
					.getProperty("MsSql.connectionString"));

		}
		connected = true;
	}

	private void checkConnection() {
		if (!connected)
			connect();
	}

	private DataSource setupDataSource(String connectURI) {
		logger.info("Creating Data Source...");
		BasicDataSource ds;
		try {
			ds = (BasicDataSource) Class.forName(
					"org.apache.commons.dbcp.BasicDataSource", true,
					Thread.currentThread().getContextClassLoader())
					.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		ds.setUsername(prop.getProperty("MsSql.username"));
		ds.setPassword(prop.getProperty("MsSql.password"));
		ds.setUrl(connectURI);

		return ds;
	}

	public static void printDataSourceStats(DataSource ds) {
		BasicDataSource bds = (BasicDataSource) ds;
		System.out.println("NumActive: " + bds.getNumActive());
		System.out.println("NumIdle: " + bds.getNumIdle());
	}

	public static void shutdownDataSource(DataSource ds) throws SQLException {
		if (ds instanceof BasicDataSource) {
			BasicDataSource bds = (BasicDataSource) ds;
			bds.close();
		}
	}

	public Connection getConnection() throws SQLException {
		checkConnection();
		Connection connection;
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			logger.error(
					"Could not get Database connection: " + e.getMessage(), e);
			throw e;
		}

		return connection;
	}

	private File baseDir;

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	public File getBaseDir() {
		return baseDir;
	}
}
