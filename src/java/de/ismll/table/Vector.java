/**
 * Vector.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.table;

// import java.io.*;
// import java.util.*;

/**
 * A generic (float) vector interface.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public interface Vector {

	/**
	 * Get the size.
	 */
	public int size();


	// ----------------------------------------------------------------------
	// Cell contents:

	/**
	 * Get the value of a cell.
	 */
	public float get(int index);

	/**
	 * Set the value of a cell.
	 */
	public void set(int index, float value);

}