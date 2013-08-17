package de.ismll.table.projections;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class ColumnSubsetVectorView implements Vector {

	private final Matrix target;
	private final int column;

	public ColumnSubsetVectorView(Matrix m, int column) {
		super();
		this.target = m;
		this.column = column;
	}

	public float get(int index) {
		return target.get(index, column);
	}

	public void set(int index, float value) {
		target.set(index, column, value);
	}

	public int size() {
		return target.getNumRows();
	}

}
