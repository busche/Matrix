package de.ismll.database.dao;

public class Column extends AbstractEntityItem {

	private final Datatypes datatype;

	//	private boolean key;

	private final Table associatedType;

	private final int ordinal;

	private int size;

	public Column(final String name, final String nativeName, final Datatypes datatype, final int ordinal, final Table associatedType) {
		super(name, nativeName);
		this.datatype = datatype;
		this.associatedType=associatedType;
		this.ordinal = ordinal;
	}

	public Column(final String name, Datatypes datatype, final int ordinal, final Table associatedType) {
		this(name, name, datatype, ordinal, associatedType);
	}

	public int getOrdinal() {
		return ordinal;
	}

	public Datatypes getDatatype() {
		return datatype;
	}

	public int getSize() {
		return size;
	}

	public Column setSize(final int size) {
		this.size = size;
		return this;
	}

	public String toString() {
		return nativeName;
		//		if (development)
		//			return "EntityField nativename=" + getNativeName() + ", name=" + getName() + ", size=" + getSize() + ", pos=" + getOrdinal();
		//
		//		return "";
	}

	public Table getAssociatedType() {
		return associatedType;
	}
}
