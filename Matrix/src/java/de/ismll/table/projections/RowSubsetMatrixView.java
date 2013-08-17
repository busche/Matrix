/**
 * RowSubsetMatrix.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.table.projections;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.ismll.table.BitVector;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.impl.DefaultMatrix;


/**
 * 
 * A simple view on a subset of the rows of a matrix.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public class RowSubsetMatrixView implements Matrix{

	protected final Matrix matrix;

	public Matrix getMatrix() {
		return matrix;
	}

	public IntVector getIndex() {
		return index;
	}

	protected final IntVector index;
	@Deprecated
	private boolean writes = true;

	@Deprecated
	public static RowSubsetMatrixView create(Matrix forMatrix, IntVector withoutIndizes) {
		IntVector iv = new DefaultIntVector(forMatrix.getNumRows());
		for (int i = 0; i < iv.size(); i++)
			iv.set(i, i);
		DefaultIntVector cp = new DefaultIntVector(withoutIndizes);
		Vectors.sort(cp);
		IntVector mask = Vectors.removeAll(iv, cp);
		return new RowSubsetMatrixView(forMatrix, mask);
	}

	/**
	 * 
	 * creates a view of matrix:<br />
	 *  split contains integers, representing split-IDs. whether or not a row is available/used in this view depends on whether the row-value in split == splitvalue.
	 * 
	 * @param matrix
	 * @param split
	 * @param splitValue
	 */
	public RowSubsetMatrixView(Matrix matrix, IntVector split, int splitValue) {
		this.matrix = matrix;
		this.index = Vectors.convert2Pointers(split, splitValue);
	}

	/**
	 * creates a view s.t. the pointers-vector points to rows in the matrix to be used/available through this view.<br />
	 * 
	 *  Example:  pointers=[1,3,6,7,8]
	 * 
	 * @param view
	 * @param pointers
	 */
	public RowSubsetMatrixView(Matrix view, IntVector pointers) {
		this(view, pointers, true);
	}



	public RowSubsetMatrixView(Matrix view, IntVector pointers, boolean writes) {
		matrix = view;
		index = pointers;
		this.writes  = writes;

	}

	/**
	 * creates a view s.t. the pointers-vector points to rows in the matrix to be used/available through this view.<br />
	 * 
	 *  Example:  pointers=[1,3,6,7,8]
	 * 
	 * @param view
	 * @param pointers
	 */
	public RowSubsetMatrixView(Matrix view, BitVector pointers) {
		super();
		matrix = view;

		index=Vectors.convert2Pointers(pointers);
	}





	public String toString() {
		return Matrices.toString(this);
	}

	/**
	 * Get the number of rows.
	 */
	public int getNumRows() { return index.size(); }

	/**
	 * Get the number of columns.
	 */
	public int getNumColumns() { return matrix.getNumColumns();}


	// ----------------------------------------------------------------------
	// Cell contents:

	/**
	 * Get the value of a cell.
	 */
	public float get(int rowIndex, int columnIndex) { return matrix.get(index.get(rowIndex), columnIndex); }

	/**
	 * Set the value of a cell.
	 */
	public void set(int rowIndex, int columnIndex, float value) {if (writes) matrix.set(index.get(rowIndex), columnIndex, value); }


}
