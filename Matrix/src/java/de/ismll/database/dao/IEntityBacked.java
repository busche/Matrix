package de.ismll.database.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import de.ismll.bootstrap.CommandLineParser;

public class IEntityBacked implements Iterator<ContentHolder>, AutoCloseable{

	private ResultSet q;
	private Connection s;
	private boolean hasNext;
	private ContentHolder ch;

	public IEntityBacked(ResultSet executeQuery, Connection toClose) throws SQLException {
		super();
		this.q = executeQuery;
		this.s = toClose;
		ResultSetMetaData metaData = executeQuery.getMetaData();
		final int ccount = metaData.getColumnCount();
		final Set<String> columns = new TreeSet<String>();
		for (int i = 0; i < ccount; i++) {
			columns.add(metaData.getColumnLabel(i+1));
		}
		ch = new ContentHolder() {

			@Override
			public int size() {
				return ccount;
			}

			@Override
			public Object put(String key, Object value) {
				// noop
				return null;
			}

			@Override
			public boolean isEmpty() {
				return ccount==0;
			}

			@Override
			public Object get(String key) {
				try {
					return q.getObject(key);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public void clear() {
				//noop
			}

			@Override
			public <T> T get(String key, Class<T> type) {
				return (T) CommandLineParser.convert(get(key), type);
			}
		};
		//		fetchNext();
	}

	private void fetchNext() {
		try {
			this.hasNext = q.next();

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	}

	public void close() {
		try {
			q.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		try {
			return q.next();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public ContentHolder next() {
		return ch;
	}

	@Override
	public void remove() {
		// not supported
	}

}
