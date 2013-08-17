package de.ismll.table.projections;

import de.ismll.table.IntMatrix;
import de.ismll.table.Matrix;

public class MatrixView implements Matrix {

	private final IntMatrix m;

	public MatrixView(IntMatrix m) {
		this.m = m;
	}


	public int getNumRows() {
		return m.getNumRows();
	}

	public int getNumColumns() {
		return m.getNumColumns();
	}


	public float get(int rowIndex, int columnIndex) {
		return m.get(rowIndex, columnIndex);
	}

	public void set(int rowIndex, int columnIndex, float value) {
		m.set(rowIndex, columnIndex, (int) value);
	}

}
