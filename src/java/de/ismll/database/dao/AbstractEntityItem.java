package de.ismll.database.dao;

public abstract class AbstractEntityItem {

	protected final String name;

	protected final String nativeName;

	public AbstractEntityItem(final String name, final String nativeName) {
		super();
		this.name = name;
		this.nativeName = nativeName;
	}

	public AbstractEntityItem(final String name) {
		this(name, name);
	}

	public String getName() {
		return name;
	}

	public String getNativeName() {
		return nativeName;
	}

}
