package de.ismll.table.projections;

import de.ismll.table.IntMatrix;
import de.ismll.table.IntVector;

public class IntVectorAsIntMatrixView implements IntMatrix {

	private final IntVector vector;

	public IntVectorAsIntMatrixView(final IntVector vector) {
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
	public int get(int rowIndex, int columnIndex) {
		return vector.get(rowIndex);
	}

	@Override
	public void set(int rowIndex, int columnIndex, int value) {
		vector.set(rowIndex, value);
	}


}
