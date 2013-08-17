package de.ismll.table.projections;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class VectorAsMatrixView implements Matrix {

	private final Vector vector;

	public VectorAsMatrixView(final Vector vector) {
		this.vector = vector;
	}

	@Override
	public int getNumRows() {
		return vector.size();
	}

	@Override
	public int getNumColumns() {
		return 1;
	}

	@Override
	public float get(int rowIndex, int columnIndex) {
		return vector.get(rowIndex);
	}

	@Override
	public void set(int rowIndex, int columnIndex, float value) {
		vector.set(rowIndex, value);
	}

}
