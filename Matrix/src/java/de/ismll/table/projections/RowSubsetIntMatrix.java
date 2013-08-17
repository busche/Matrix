package de.ismll.table.projections;

import de.ismll.table.BitVector;
import de.ismll.table.IntMatrix;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultIntVector;

public class RowSubsetIntMatrix implements IntMatrix {



	private final IntMatrix target;
	private IntVector index;

	public RowSubsetIntMatrix(IntMatrix target, BitVector mask) {
		super();
		this.target = target;
		index=Vectors.convert2Pointers(mask);
	}

	/**
	 * creates a view s.t. the pointers-vector points to rows in the matrix to be used/available through this view.<br />
	 * 
	 *  Example:  pointers=[1,3,6,7,8]
	 * 
	 * @param view
	 * @param pointers
	 */
	public RowSubsetIntMatrix(IntMatrix view, IntVector pointers) {
		super();
		target = view;
		index = pointers;
	}

	public String toString() {
		return Matrices.toString(this);
	}


	public static RowSubsetIntMatrix create(IntMatrix forMatrix, IntVector withoutIndizes) {
		IntVector iv = new DefaultIntVector(forMatrix.getNumRows());
		for (int i = 0; i < iv.size(); i++)
			iv.set(i, i);
		DefaultIntVector cp = new DefaultIntVector(withoutIndizes);
		Vectors.sort(cp);
		IntVector mask = Vectors.removeAll(iv, cp);
		return new RowSubsetIntMatrix(forMatrix, mask);
	}

	/**
	 * Get the number of rows.
	 */
	public int getNumRows() { return index.size(); }

	/**
	 * Get the number of columns.
	 */
	public int getNumColumns() { return target.getNumColumns();}


	public Integer getExpensive(int row, int col) {

		return Integer.valueOf(get(row, col));
	}
	// ----------------------------------------------------------------------
	// Cell contents:

	/**
	 * Get the value of a cell.
	 */
	public int get(int rowIndex, int columnIndex) { return target.get(index.get(rowIndex), columnIndex); }

	/**
	 * Set the value of a cell.
	 */
	public void set(int rowIndex, int columnIndex, int value) { target.set(index.get(rowIndex), columnIndex, value); }

}
