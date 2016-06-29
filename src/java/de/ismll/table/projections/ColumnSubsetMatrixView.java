package de.ismll.table.projections;

import de.ismll.table.BitVector;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultIntVector;

public class ColumnSubsetMatrixView implements Matrix {

	private Matrix target;
	private final IntVector columns2Use;

	/**
	 * @param pointers [0,2,3,4] to use the names columns (excludes column 1 for this view)
	 */
	public ColumnSubsetMatrixView(Matrix target, int[] pointers) {
		this(target, new DefaultIntVector(pointers));
	}

	public ColumnSubsetMatrixView(Matrix target, BitVector mask) {
		super();
		this.target = target;
		this.columns2Use = Vectors.convert2Pointers(mask);
	}

	public ColumnSubsetMatrixView(Matrix data, Vector attributes) {
		this(data, new DefaultIntVector(attributes));
	}

	public ColumnSubsetMatrixView(Matrix target2,
			IntVector defaultIntVector) {
		target = target2;
		columns2Use = defaultIntVector;
	}

	public float get(int rowIndex, int columnIndex) {
		return target.get(rowIndex, columns2Use.get(columnIndex));
	}

	public int getNumColumns() {
		return columns2Use.size();
	}

	public int getNumRows() {
		return target.getNumRows();
	}

	public void set(int rowIndex, int columnIndex, float value) {
		target.set(rowIndex, columns2Use.get(columnIndex), value);
	}


	public String toString() {
		return Matrices.toString(this);
	}

	public static ColumnSubsetMatrixView create(Matrix data, int[] is) {
		return create(data, DefaultIntVector.wrap(is));
	}

	public static ColumnSubsetMatrixView create(Matrix forMatrix, IntVector withoutIndizes) {
		IntVector iv = new DefaultIntVector(forMatrix.getNumColumns());
		for (int i = 0; i < iv.size(); i++)
			iv.set(i, i);
		DefaultIntVector cp = new DefaultIntVector(withoutIndizes);
		Vectors.sort(cp);
		IntVector mask = Vectors.removeAll(iv, cp);
		return new ColumnSubsetMatrixView(forMatrix, mask);
	}


}
