package de.ismll.database.dao;

import java.sql.SQLException;

public class DataStoreException extends Exception {

	public DataStoreException(String string) {
		super(string);
	}

	public DataStoreException(SQLException e) {
		super(e);
	}

}
