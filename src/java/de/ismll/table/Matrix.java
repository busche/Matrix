/**
 * Matrix.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.table;

/**
 * A generic (float) matrix interface.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public interface Matrix {

	/**
	 * Get the number of rows.
	 */
	public int getNumRows();

	/**
	 * Get the number of columns.
	 */
	public int getNumColumns();


	/**
	 * Get the value of a cell.
	 */
	public float get(int rowIndex, int columnIndex);

	/**
	 * Set the value of a cell.
	 */
	public void set(int rowIndex, int columnIndex, float value);

}