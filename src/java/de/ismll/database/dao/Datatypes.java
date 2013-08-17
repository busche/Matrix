package de.ismll.database.dao;

public enum Datatypes {
	String(String.class), Integer(Integer.class), Double(Double.class), VString(String.class);

	private Class<?> java;

	Datatypes(Class<?> java) {
		this.java = java;

	}

	public Class<?> getJavaClass() {
		return java;
	}

}
