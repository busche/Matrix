/**
 * Vector.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.table;

/**
 * A generic (int) vector interface.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public interface IntVector {

	/**
	 * Get the size.
	 */
	public int size();

	/**
	 * Get the value of a cell.
	 */
	public int get(int index);

	/**
	 * Set the value of a cell.
	 */
	public void set(int index, int value);

}