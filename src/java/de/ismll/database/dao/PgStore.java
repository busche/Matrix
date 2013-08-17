package de.ismll.database.dao;

import java.sql.SQLException;

import de.ismll.database.SqlStatement;
import de.ismll.database.pgsql.PostgresSQL;

public class PgStore extends SqlStore {

	private PostgresSQL postgresSQL;

	public PgStore(PostgresSQL postgresSQL) {
		this.postgresSQL = postgresSQL;
	}

	//	@Override
	//	public IEntityBacked query(SqlStatement arg0, Table type)
	//			throws DataStoreException {
	//		// TODO Auto-generated method stub
	//
	//		return null;
	//	}

	@Override
	public IEntityBacked query(SqlStatement sql) throws DataStoreException {

		return postgresSQL.query(sql);
	}

	//	@Override
	//	public Entity queryByStatement(SqlStatement source, Entity entity)
	//			throws DataStoreException, DataStoreException {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}

	@Override
	public long executeNonQuery(SqlStatement arg0, boolean writes,
			boolean isDdlCommand, boolean retrieveKey) throws DataStoreException {
		try {
			return 	postgresSQL.executeNonQuery(arg0, writes, isDdlCommand, retrieveKey);
		} catch (SQLException e) {
			throw new DataStoreException(e);
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	protected String nativeType(Datatypes datatype, int size) {
		switch (datatype) {
		case Integer:
			return "integer";
		case String:
			return "varchar(" + size + ")";
		case VString:
			return "character varying";
		case Double:
			return "double precision";

		}
		throw new RuntimeException("Unhandeled data type: " + datatype);
	}

}
