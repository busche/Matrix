package de.ismll.table.io.weka;


public interface ArffEncoder {

	enum Type{
		Numeric, Nominal
	}

	public  static final String NUMERIC = "NUMERIC";

	//	/**
	//	 * @param column
	//	 * @return such as NUMERIC, or {Red,Blue,Green}
	//	 */
	//	String getWekaTypeDescription(int column);

	/**
	 * @param column
	 * @return such as NUMERIC, or {Red,Blue,Green}
	 */
	Type getAttributeType(int column);


	/**
	 * @param column
	 * @return arbitrary name.
	 */
	String getAttributeName(int column);

	String encode(int column, float value);

	String getName();

	//	Map<Integer, String> getMap(int column);

}
