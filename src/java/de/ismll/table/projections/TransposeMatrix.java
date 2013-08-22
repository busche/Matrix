package de.ismll.table.projections;

import de.ismll.table.Matrix;

public class TransposeMatrix implements Matrix{

	private Matrix toTranspose;

	@Override
	public int getNumRows() {
		return toTranspose.getNumColumns();
	}

	@Override
	public int getNumColumns() {
		return toTranspose.getNumRows();
	}

	@Override
	public float get(int rowIndex, int columnIndex) {
		return toTranspose.get(columnIndex, rowIndex);
	}

	@Override
	public void set(int rowIndex, int columnIndex, float value) {
		toTranspose.set(columnIndex, rowIndex, value);
	}

	public TransposeMatrix(Matrix toTranspose) {
		this.toTranspose = toTranspose;

	}
}
