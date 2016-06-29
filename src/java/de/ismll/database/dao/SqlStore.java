package de.ismll.database.dao;

import java.sql.SQLException;
import java.util.Vector;

import de.ismll.bootstrap.CommandLineParser;
import de.ismll.database.ISqlStatementSource;
import de.ismll.database.SqlStatement;


public abstract class SqlStore extends DataStore{

	protected ISqlStatementSource getCreateTableStatement(final Table type) {
		System.out.println("getCreateTableStatement called");
		final StringBuffer sb = new StringBuffer();
		final String tablename = type.getNativeName();
		final Column[] sortedField = type.getSortedField();
		final int length = sortedField.length;

		final Column keyField = type.getKeyField();
		sb.append("CREATE TABLE  ");//IF NOT EXISTS
		sb.append(tablename);
		sb.append("(");
		sb.append(keyField.getNativeName());
		sb.append(" serial , PRIMARY KEY (" + keyField.getNativeName() + ")");

		for (int i = 0 ; i < length; i++) {
			final Column field = sortedField[i];
			sb.append(", ");
			sb.append(field.getNativeName());
			sb.append(" ");
			sb.append(nativeType(field.getDatatype(), field.getSize()));

		}
		//
		//		for (int i = 0; i <fieldsSize ; i++) {
		//			final EntityField field = (EntityField) fields.elementAt(i);
		//			sb.append(", ");
		//			sb.append(field.getNativeName());
		//			sb.append(" ");
		//			sb.append(ddlFieldType(field.getDatatype(), field.getSize()));
		//
		//		}


		sb.append(")");
		return new SqlStatement(sb.toString());
	}


	protected abstract String nativeType(Datatypes datatype, int size);


	//	public Entity query(final Table type, final long id) throws DataStoreException, DataStoreException {
	//		// SELECT * FROM type.getTableName() WHERE _id=id;
	//		final String tablename = type.getNativeName();
	//
	//		final StringBuffer sb = new StringBuffer();
	//		sb.append("SELECT * FROM ");
	//		sb.append(tablename);
	//		sb.append(" WHERE ");
	//		sb.append(type.getKeyField().getNativeName());
	//		sb.append("=");
	//		sb.append(id);
	//		final SqlStatement stmt = new SqlStatement(sb.toString());
	//		return queryByStatement(stmt, type.createInstance());
	//
	//	}


	//	public abstract IEntityBacked query(final SqlStatement arg0, Table type) throws DataStoreException;

	public IEntityBacked query(final Table type) throws DataStoreException, DataStoreException {
		return query(type, new Column[0], new String[0],(Column[])null, (String[])null);
	}



	public IEntityBacked query(final Table entityType,
			final Column[] nativeFieldNames,
			final Object[] requiredFieldValues,
			final Column order,
			final String how) throws DataStoreException {
		if (order == null) {
			return query(entityType, nativeFieldNames, requiredFieldValues, (Column[])null, (String[])null);
		}
		return query(entityType, nativeFieldNames, requiredFieldValues, new Column[] {order}, new String[] {how});

	}

	public IEntityBacked query(final Table entityType,
			final Column[] nativeFieldNames,
			final Object[] requiredFieldValues,
			final Column[] order,
			final String[] how) throws DataStoreException {
		final int length;
		if (nativeFieldNames !=null)
			length= nativeFieldNames.length;
		else
			length=0;

		final String tablename = entityType.getNativeName();

		final StringBuffer sb = new StringBuffer();

		sb.append("SELECT * FROM ");
		sb.append(tablename);
		boolean whereAdded=false;
		boolean first=true;

		for(int i = length-1; i >= 0; i--) {
			if (!first)
				sb.append(" AND " );
			if (!whereAdded) {
				sb.append(" WHERE ");
				whereAdded=true;
			}
			final Datatypes datatype = nativeFieldNames[i].getDatatype();
			sb.append(nativeFieldNames[i].getNativeName());
			sb.append("=");
			switch (datatype) {
			case String:
			case VString:
				sb.append("'");
				try {
					org.postgresql.core.Utils.appendEscapedLiteral(sb, requiredFieldValues[i].toString(), true);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sb.append("'");
				break;
			default:
				sb.append(requiredFieldValues[i]);
				break;
			}
			first = false;
		}

		boolean orderSeparatorNeedsToBeAdded=false;
		boolean orderByNeeds2BeAdded=true;


		if (order != null ) {
			final int orderLength = order.length;
			for (int i = 0; i < orderLength; i++) {
				final Column f = order[i];
				if (f == null)
					continue;
				if (orderSeparatorNeedsToBeAdded)
					sb.append(", ");
				if(orderByNeeds2BeAdded) {
					sb.append(" ORDER BY ");
					orderByNeeds2BeAdded=false;
				}
				sb.append(f.getNativeName());
				if (how != null && how[i]!=null) {
					sb.append(" ");
					sb.append(how[i]);
				}
				orderSeparatorNeedsToBeAdded=true;
			}
		}

		final SqlStatement stmt = new SqlStatement(sb.toString());
		return query(stmt);

	}

	/**
	 * aka. SELECT fields from entitytype where conditions in parameter object>
	 * 
	 */
	public IEntityBacked query(final ISqlStatementSource sql) throws DataStoreException  {
		return query(sql.getStatement());
	}

	/**
	 * aka. SELECT fields from entitytype where conditions in parameter object
	 * 
	 */
	public abstract IEntityBacked query(SqlStatement sql) throws DataStoreException;

	/**
	 * for Android-SQLite
	 * 
	 * insert table, nullColumnHack, ContentValues
	 * 
	 * update table, contentValues, where, whereArgs[]
	 * 
	 * delete table, where, whereArgs[]
	 * 
	 * @return either the amount of affected rows, or the inserted ID
	 */
	public long executeNonQuery(final ISqlStatementSource arg0, final boolean writes, final boolean isDdlCommand,boolean retrieveKey) throws DataStoreException, DataStoreException {
		return executeNonQuery(arg0.getStatement(), writes, isDdlCommand, retrieveKey);
	}

	public abstract long executeNonQuery(final SqlStatement arg0, final boolean writes, final boolean isDdlCommand,boolean retrieveKey) throws DataStoreException, DataStoreException;

	public void ensureTableExists(final Table type) throws DataStoreException, DataStoreException {
		System.out.println("ensureTableExists called");

		if (existingTables.contains(type))
			return; // already loaded.
		
		
		
		// TODO: consider adding some kind of updating methodology when schema is enhanced
		SqlStatement sql = new SqlStatement("SELECT COUNT (relname) as a FROM pg_class WHERE relname = '" + type.nativeName + "'");
		IEntityBacked query = query(sql);
		boolean exists=false;
		if (query.hasNext()) {
			IContentHolder next = query.next();
			Integer object = next.get("a", Integer.class);
			System.out.println(object);
			if (object.intValue()>0) exists=true;
			System.out.println(exists);
			query.close();
		}
		if (!exists) {
			final ISqlStatementSource stmt = getCreateTableStatement(type);
			executeNonQuery(stmt, true, true, false);
		}
		existingTables.addElement(type);
	}

	private static Vector existingTables;

	static {
		existingTables = new Vector();
	}


	public long insertOrUpdate(final Entity e) throws DataStoreException {
		// TODO: implement UPDATE!!!!
		if (e.id>0) {
			throw new DataStoreException("UPDATE not implemented!!!");
		}
		final Table entityType = e.getEntityType();
		final StringBuffer sb = new StringBuffer();
		final String nativeName = entityType.getNativeName();
		sb.append("INSERT INTO ");
		sb.append(nativeName);
		sb.append("(");
		final Vector<Column> fields = entityType.getFields();
		final int size = fields.size();
		for (int i = size-1; i>=0; i--) {
			sb.append((fields.elementAt(i)).getNativeName());
			if (i>0)
				sb.append(",");
		}
		sb.append(") VALUES (");

		final Object[] values = new Object[size];

		for (int i = size-1; i>=0; i--) {
			final Column field = fields.elementAt(i);
			sb.append("?");

			if (i>0)
				sb.append(",");
			Object sourceObject = e.get(field);
			Class<?> targetClazz = field.getDatatype().getJavaClass();
			//			if (sourceObject.getClass().equals(targetClazz)) {
			//				values[size-i-1] = sourceObject;
			//			} else {
			if (sourceObject != null )
				values[size-i-1] = CommandLineParser.convert(sourceObject, targetClazz);
			//			}
		}

		sb.append(")");

		final long ret =  executeNonQuery(new SqlStatement(sb.toString(), "insert::" + nativeName, values), true, false, true);

		if (ret > 0) {
			e.id=ret;
		}

		return ret;
	}

	//
	//	protected String[] asStringArray(final Object[] p) {
	//		final String[] ret = new String[p.length];
	//		for (int i = p.length-1; i >= 0; i--) {
	//			if (p[i] instanceof String)
	//				ret[i] = (String)p[i];
	//			else
	//				ret[i] = p[i].toString();
	//
	//		}
	//		return ret;
	//	}

	//	protected Table constructEntityType(final String[] columnNames,
	//			final String entityTypeName) {
	//		final int columnCount = columnNames.length;
	//		final Table shadowType = new Table(entityTypeName);
	//
	//		for (int i = columnCount-1; i>=0; i--) {
	//			final Column field = shadowType.addColumn(columnNames[i], Datatypes.STRING, i);
	//
	//		}
	//		return shadowType;
	//	}


}

