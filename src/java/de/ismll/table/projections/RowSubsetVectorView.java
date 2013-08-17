package de.ismll.table.projections;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class RowSubsetVectorView implements Vector {

	private final Matrix target;
	private final int row;

	public RowSubsetVectorView(Matrix m, int row) {
		super();
		this.target = m;
		this.row = row;
	}

	public float get(int index) {
		return target.get(row, index);
	}

	public void set(int index, float value) {
		target.set(row, index, value);
	}

	public int size() {
		return target.getNumColumns();
	}

}
