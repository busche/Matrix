package de.ismll.database.dao;

import java.util.Vector;

public class Table extends AbstractEntityItem {

	//	public static final int FORMAT_LONG=0;
	//	public static final int FORMAT_MEDIUM=1;
	//	public static final int FORMAT_SHORT=2;

	private final Vector<Column> fields;
	//	private final Class instanceType;

	public Table(final String name, final String nativeName) {
		super(name, nativeName);
		fields = new Vector<Column>();
		KEY_FIELD =  new Column("_id", Datatypes.Integer, -1, this);
	}

	public Table(final String name) {
		this(name, name);
	}


	public Vector<Column> getFields() {
		return fields;
	}

	public Column addColumn(final String name, final String nativeName, final Datatypes datatype, final int ordinal) {
		final Column f = new Column(name, nativeName, datatype, ordinal, this);
		fields.add(f);
		return f;
	}

	public Column addColumn(final String name, final Datatypes string, final int ordinal) {
		return addColumn(name, name, string, ordinal);
	}

	public Column addColumn(final String name, final Datatypes string) {
		return addColumn(name, name, string, fields.size());
	}

	/**
	 * Key Field
	 */
	public final Column KEY_FIELD;

	public Column getKeyField() {
		return KEY_FIELD;
	}

	public Entity createInstance() {
		return new GenericEntity(this);
	}

	//	private static Hashtable entityTypes;
	//
	//	public static int getNumRegisteredEntities() {
	//		return entityTypes.size();
	//	}
	//
	//	static {
	//		entityTypes = new Hashtable();
	//	}

	//	private static int typeIdCounter=0;
	public int typeId=-1;

	//	public static void addEntityType(final EntityType t) {
	//		if (!entityTypes.contains(t)) {
	//			entityTypes.put(t.getInstanceType().getName(), t);
	//			t.typeId=typeIdCounter++;
	//		}
	//
	//	}

	//	public static EntityType getEntityType(final Class c, final boolean throwIfNotFound) {
	//		final Enumeration elements = entityTypes.elements();
	//
	//		while (elements.hasMoreElements()) {
	//			final EntityType current = (EntityType) elements.nextElement();
	//			if (current.getInstanceType().equals(c))
	//				return current;
	//		}
	//		if (throwIfNotFound)
	//			throw new RuntimeException("EntityType not found - registered?");
	//		return null;
	//	}

	//	public static EntityType getEntityType(final Class c) {
	//		return getEntityType(c, false);
	//	}


	public Column getField(final String key, final boolean nativeName) {
		final Vector<Column> f = fields;
		final int size = f.size();
		for (int i = size-1; i >= 0; i--) {
			final Column current = f.elementAt(i);
			if (key.equals((nativeName?current.getNativeName():current.getName())))
				return current;
		}
		return null;
	}

	//	public static EntityType getEntityType(final int typeId) {
	//		final Enumeration elements = entityTypes.elements();
	//
	//		while (elements.hasMoreElements()) {
	//			final EntityType current = (EntityType) elements.nextElement();
	//			if (current.typeId == typeId)
	//				return current;
	//		}
	//		throw new RuntimeException("EntityType with id " + typeId + " not found - registered?");
	//
	//	}

	private Column[] sortedField;

	public Column[] getSortedField() {
		if (sortedField != null)
			return sortedField;
		sortedField = new Column[fields.size()];
		for (int i = 0; i < fields.size(); i++)
			sortedField[i] = fields.elementAt(i);
		final int length = sortedField.length;
		for (int i = 0; i < length; i++) {
			for (int j = i+1; j < length; j++) {
				if (sortedField[i].getOrdinal()>sortedField[j].getOrdinal()) {
					final Column tmp = sortedField[i];
					sortedField[i]=sortedField[j];
					sortedField[j]=tmp;
				}
			}
		}
		return sortedField;
	}

	public void addColumns(String[] strings, Datatypes type) {
		for (String s : strings)
			addColumn(s, type);
	}
}
