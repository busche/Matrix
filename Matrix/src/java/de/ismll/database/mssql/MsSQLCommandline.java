package de.ismll.database.mssql;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Formatter;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class MsSQLCommandline {

	enum SQL_Query_Type{
		INSERT, UPDATE, DELETE, SELECT
	}

	static Logger logger = LogManager.getLogger(MsSQLCommandline.class);

	static PrintStream out;
	static boolean closeOut = false;

	private static String pattern;

	private static boolean header;

	private static boolean newlineAware = false;

	public static void main(String[] args) {

		if (args.length<1) {
			logger.fatal("Need at least one parameter: the query!");
			logger.info("Supported command line parameters:");
			logger.info("-t <SQL Query type>  : one of " + Arrays.toString(SQL_Query_Type.values()));
			logger.info("-f <file-name>       : read query from a file");
			logger.info("-o <file-name>       : write results to a file (instead of std-out)");
			logger.info("-h                   : include header (column names)");
			logger.info("-p <format-pattern>  : a pattern passed to String.format() in order to format values");
			System.exit(1);
			return;
		}

		SQL_Query_Type type = SQL_Query_Type.INSERT;
		File commandos=null;
		File outputFile=null;
		out=System.out;

		for (int i = 0; i < args.length-1; i++) {
			char command=' ';

			if (args[i].startsWith("-"))
				command= args[i].charAt(1);

			switch (command) {
			case 't':
				type = SQL_Query_Type.valueOf(args[i+1]);
				i++;
				break;
			case 'f':
				commandos=new File(args[i+1]);
				if (!commandos.isFile())
					throw new RuntimeException(commandos + " is no file!");
				i++;
				break;
			case 'o':
				outputFile=new File(args[i+1]);
				i++;
				closeOut = true;
				break;
			case 'p':
				pattern = args[i+1];
				i++;
				break;
			case 'h':
				header=true;
				break;
			case 'n':
				newlineAware=true;
				break;
			}

		}
		if (outputFile!=null) {
			try {
				outputFile.createNewFile();
				out = new PrintStream(outputFile);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}

		}

		logger.debug("Creating MsSQL Object ...");
		MsSQL ms;
		try {
			ms = new MsSQL();
		} catch (IOException e) {
			logger.fatal(e.getMessage(), e);
			System.exit(2);
			return;
		}
		logger.debug("Getting connection ...");
		Connection connection;
		try {
			connection = ms.getConnection();
		} catch (SQLException e) {
			logger.fatal(e.getMessage(), e);
			System.exit(3);
			return;
		}

		Statement createStatement;
		try {
			createStatement = connection.createStatement();
		} catch (SQLException e) {
			logger.fatal(e.getMessage(), e);
			if (connection!=null)
				try {
					connection.close();
				} catch (SQLException e1) {
					logger.debug(e1.getMessage(),e1);
				}
			System.exit(3);
			return;
		}
		String sql;
		if (commandos!=null) {
			FileReader fr;
			try {
				fr = new FileReader(commandos);
				LineNumberReader read = new LineNumberReader(fr);
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = read.readLine())!=null) {
					sb.append(line + " ");
				}
				sql = sb.toString();
				if (sql.length()<10000)
					logger.info("Read query from file: " + sql);
				else
					logger.info("Query exceeds 10000 chars. Not displaying on console.");
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
				System.exit(7);
				throw new RuntimeException(e);
			}

		} else {
			sql=args[args.length-1];
			if (sql.length()<10000)
				logger.debug("Query from command line: " + sql);
			else
				logger.info("Query exceeds 10000 chars. Not displaying in logs.");
		}


		try {
			work(type, createStatement, sql);
		} catch (SQLException e) {

			logger.fatal(e.getMessage(), e);
			System.exit(4);
			return;
		} finally {
			try {
				createStatement.close();
			} catch (SQLException e) {
				logger.debug(e.getMessage(), e);
			}
			try {
				connection.close();
			} catch (SQLException e) {
				logger.debug(e.getMessage(), e);
			}
			if (closeOut)
				out.close();
		}
	}

	private static void work(SQL_Query_Type type, Statement createStatement, String sql) throws SQLException {

		if (newlineAware) {
			logger.info("Newline-aware switch -n given. Replacing all occurrences of <<newline>> in SQL query with \\n");
			sql = sql.replaceAll("<<newline>>", "\n");
		}

		switch (type) {
		case INSERT:
			int executeUpdate = createStatement.executeUpdate(sql);
			logger.info(String.format("INSERT affected %1$s rows.", executeUpdate + ""));
			break;
		case DELETE:
			logger.fatal("Not implemented: DELETE!");
			System.exit(9);
			break;
		case UPDATE:
			logger.fatal("Not implemented: UPDATE!");
			System.exit(10);
			break;
		case SELECT:
			ResultSet executeQuery = createStatement.executeQuery(sql);
			ResultSetMetaData metaData = executeQuery.getMetaData();
			int columnCount = metaData.getColumnCount();
			if(header) {
				for (int i = 0; i < columnCount; i++) {
					if (i>0)
						out.print("\t");
					out.print(metaData.getColumnName(i+1));
				}
				out.println();
			}
			Formatter f = new Formatter(out);
			while (executeQuery.next()) {
				if (pattern==null) {
					for (int i = 0; i < columnCount; i++) {
						if (i>0)
							f.format("\t");
						//							out.print("\t");
						Object obj = executeQuery.getObject(i+1);
						f.format("%1$s", obj);
						//						out.print((obj==null?"null":obj.toString()));
					}
					f.format("%n");
					//					-p "%1$s;%2$s;%3$s%n" -o magnaune.excerpt.grouing
					//					out.println();
				} else {
					// pattern != null
					Object[] values = new Object[columnCount];
					for (int i = 0; i < columnCount; i++) {
						Object obj = executeQuery.getObject(i+1);
						values[i] = ((obj==null?"null":obj));
					}
					f.format(pattern, values);
				}
			}
			break;
		}
	}
}
