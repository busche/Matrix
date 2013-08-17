package de.ismll.table.projections;

import de.ismll.table.IntMatrix;
import de.ismll.table.IntVector;
import de.ismll.table.Matrix;

public class ColumnSubsetIntVectorView implements IntVector {

	private IntMatrix target;
	private final int column;
	private Matrix mf;

	private final boolean useInts;
	public ColumnSubsetIntVectorView(IntMatrix m, int column) {
		super();
		this.target = m;
		useInts=true;
		this.column = column;
	}

	public ColumnSubsetIntVectorView(Matrix mf, int column2) {
		this.mf = mf;
		useInts=false;
		column = column2;
	}

	public int get(int index) {
		if (useInts)
			return target.get(index, column);
		return (int) mf.get(index, column);
	}

	public void set(int index, int value) {
		if (useInts)
			target.set(index, column, value);
		else
			mf.set(index, column, value);
	}

	public int size() {
		if (useInts)
			return target.getNumRows();
		return mf.getNumRows();
	}

}
