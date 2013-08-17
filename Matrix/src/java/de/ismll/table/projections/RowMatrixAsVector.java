package de.ismll.table.projections;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;

/**
 * 
 * do not use directly; use {@link Matrices#row(Matrix, int)} instead.
 * 
 * @author Andre Busche
 *
 */
@Deprecated
public class RowMatrixAsVector implements Vector {

	private final Matrix m;
	private final int row;

	public RowMatrixAsVector(Matrix m, int row) {
		this.m = m;
		this.row = row;
	}

	@Override
	public int size() {
		return m.getNumColumns();
	}

	@Override
	public float get(int index) {
		return m.get(row, index);
	}

	@Override
	public void set(int index, float value) {
		m.set(row, index, value);
	}

}
