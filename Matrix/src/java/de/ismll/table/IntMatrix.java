package de.ismll.table;

public interface IntMatrix {


	// ----------------------------------------------------------------------
	// Cell contents:

	/**
	 * Get the value of a cell.
	 */
	public int get(int rowIndex, int columnIndex);

	/**
	 * Set the value of a cell.
	 */
	public void set(int rowIndex, int columnIndex, int value);

	/**
	 * Get the number of rows.
	 */
	public int getNumRows();

	/**
	 * Get the number of columns.
	 */
	public int getNumColumns();
}
