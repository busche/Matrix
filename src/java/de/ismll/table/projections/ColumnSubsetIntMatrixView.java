package de.ismll.table.projections;

import de.ismll.table.BitVector;
import de.ismll.table.IntMatrix;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultIntVector;

public class ColumnSubsetIntMatrixView implements IntMatrix {

	private IntMatrix target;
	private final IntVector columns2Use;

	/**
	 * @param pointers [0,2,3,4] to use the names columns (excludes column 1 for this view)
	 */
	public ColumnSubsetIntMatrixView(IntMatrix target, int[] pointers) {
		super();
		this.target = target;
		this.columns2Use = new DefaultIntVector(pointers);
	}

	public ColumnSubsetIntMatrixView(IntMatrix target, BitVector mask) {
		super();
		this.target = target;
		this.columns2Use = Vectors.convert2Pointers(mask);
	}

	public int get(int rowIndex, int columnIndex) {
		return target.get(rowIndex, columns2Use.get(columnIndex));
	}

	public int getNumColumns() {
		return columns2Use.size();
	}

	public int getNumRows() {
		return target.getNumRows();
	}

	public void set(int rowIndex, int columnIndex, int value) {
		target.set(rowIndex, columns2Use.get(columnIndex), value);
	}


	public String toString() {
		return Matrices.toString(this);
	}

	public Integer getExpensive(int row, int col) {
		return Integer.valueOf(get(row, col));
	}

}
