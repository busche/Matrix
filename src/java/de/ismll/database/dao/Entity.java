package de.ismll.database.dao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.bootstrap.CommandLineParser;

/**
 * an entity itself is a ContentHolder, backed with non-native entity field names.
 * 
 * @author Busche
 *
 */
public abstract class Entity implements IContentHolder{

	private static Logger logger = LogManager.getLogger(Entity.class);


	private Column KEY_FIELD;

	private Table type;

	private static int ID_COUNTER = 0;

	public final Object[] data;

	Map<String, Column> lookup;

	protected Entity(final Table type) {
		super();
	
		this.type = type;
		KEY_FIELD=type.getKeyField();
		
		Vector<Column> fields = type.getFields();
		lookup=new HashMap<String, Column>(fields.size()+1);
		for (Column c : fields) {
			lookup.put(c.nativeName, c);
		}

		this.data = new Object[type.getFields().size()];

		init();
	}


	/**
	 * calls {@link #setValues(IContentHolder, boolean)} while using normal (non-native) names.
	 */
	public void setValues(final IContentHolder cache) {
		setValues(cache, this.data, false);
	}

	/**
	 * sets the values as given in cache to this entity.
	 * 
	 * if nativeName is true, the keys in cache are mapped according to the {@link Column#getNativeName()} native names of the entity fields,
	 * else if native name is false, the {@link Column#getName()} is used to map key-value pairs while setting properties
	 * 
	 */
	public void setValues(final IContentHolder cache, final boolean nativeNames) {
		setValues(cache, this.data, nativeNames);
	}

	private Object[] setValuesImpl(final IContentHolder cache, final boolean nativeNames) {
		final Vector fields = type.getFields();
		final int size = fields.size();
		final Object[] d;
		if (this.data == null) {
			d = new String[size];
		} else {
			d = this.data;
		}
		setValues(cache, d, nativeNames);
		return d;
	}

	private final void setValues(final IContentHolder cache, final Object[] d, final boolean nativeNames) {
		final Vector<Column> fields = type.getFields();
		final int size = fields.size();
		for (int i = size-1; i >= 0; i--) {
			final Column ef = fields.elementAt(i);

			d[ef.getOrdinal()] = cache.get((nativeNames?ef.getNativeName():ef.getName()));
		}
	}


	protected abstract Table registerType();

	public Table getEntityType() {
		return type;
	}

	public long id=-1;


	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;

	}

	public Object get(final int field) {
		if (field>=data.length || field < 0)
			return null;
		return data[field];
	}

	public Object get(final String key) {
		return get(key, false);
	}


	@Override
	public <T> T get(String key, Class<T> type) {
		return (T) CommandLineParser.convert(get(key), type);
	}

	public Object get(final String key, final boolean nativeName) {
		return get(type.getField(key, nativeName));
	}

	public Object get(final Column field) {
		return get(field.getOrdinal());
	}


	public void init() {
		for (int i = 0; i < data.length; i++) {
			data[i] = null;
		}
	}

	public String getPayload(final int identifier) {
		return null;
	}

	public static int createId() {
		return ID_COUNTER++;
	}

	public boolean isEmpty() {
		return getId()<0;
	}

	public Object put(final String key, final Object value) {
		final Column field = type.getField(key, false);
		Object old;
		if (field == KEY_FIELD) {
			old = getId() + "";
			// overriding ID!?!?!
			System.err.println("Overriding ID not yet supported!");
		} else {
			old = this.data[type.getField(key, false).getOrdinal()];
			final int ordinal = field.getOrdinal();
			if (ordinal>=0)
				this.data[ordinal] = (value instanceof String?(String)value:value.toString());
		}
		return old;
	}

	public int size() {
		return data.length+1; // plus KEY field...
	}

	public void clear() {
		init();
	}


	public boolean isNew() {
		// TODO: revise!
		return getId()<0;
	}

	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Entity of type ");
		sb.append(type.getNativeName());
		sb.append(": id=");
		sb.append(id);
		sb.append(";data=");
		sb.append(Arrays.toString(data));

		return sb.toString();
	}

	public void set(final Column field, final Object value) {
		data[field.getOrdinal()] = value;
	}

	public void set(String nativeColumnName, Object value) {
		Column column = lookup.get(nativeColumnName);
		if (column==null) {
			logger.warn("column " + nativeColumnName + " not found!");
			return;
		}
		data[column.getOrdinal()] = value;
	}

}
