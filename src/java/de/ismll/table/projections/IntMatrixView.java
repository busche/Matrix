package de.ismll.table.projections;

import de.ismll.table.IntMatrix;
import de.ismll.table.IntVector;
import de.ismll.table.Matrix;

public class IntMatrixView implements IntMatrix {

	private Matrix m;
	private IntVector v;

	public IntMatrixView(Matrix m) {
		this.m = m;
	}

	public IntMatrixView(IntVector v) {
		this.v = v;
	}



	public int getNumRows() {
		if (v != null)
			return v.size();
		return m.getNumRows();
	}

	public int getNumColumns() {
		if (v != null)
			return 1;
		return m.getNumColumns();
	}

	public Integer getExpensive(int row, int col) {
		if (v != null)
			return Integer.valueOf(v.get(row));
		return Integer.valueOf((int) m.get(row, col));
	}

	public int get(int rowIndex, int columnIndex) {
		if (v != null)
			return v.get(rowIndex);
		return (int) m.get(rowIndex, columnIndex);
	}

	public void set(int rowIndex, int columnIndex, int value) {
		if (v != null)
			v.set(rowIndex, value);
		else
			m.set(rowIndex, columnIndex, value);
	}

}
