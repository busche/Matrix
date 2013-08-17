package de.ismll.table.projections;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class ColumnUnionMatrixView implements Matrix{

	private final Matrix[] matrices;
	private int numRows;
	private int[] columnSplitPoints;
	private int numColumns;

	public ColumnUnionMatrixView(Matrix[] matrices) {
		super();
		this.matrices = matrices;
		if (matrices.length<1)
			throw new IllegalArgumentException("need at lease one matrix!");
		numRows = matrices[0].getNumRows();
		columnSplitPoints = new int[matrices.length-1];
		int delta=0;
		for (int i = 1; i < matrices.length; i++){
			columnSplitPoints[i-1] = delta + matrices[i-1].getNumColumns()-1;
			delta += matrices[i-1].getNumColumns();

			if (numRows != matrices[i].getNumRows())
				throw new IllegalArgumentException("Rows mismatch. Matrices need to have the same amount of rows (" + numRows  + "!=" + matrices[i].getNumRows() + ", checked index " + i + ")!");
		}
		this.numColumns = matrices[matrices.length-1].getNumColumns()+delta;
	}

	public ColumnUnionMatrixView(Vector[] vectors) {
		this(wrap(vectors));
	}

	private static Matrix[] wrap(Vector[] vectors) {
		Matrix[] ret=  new Matrix[vectors.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = new VectorAsMatrixView(vectors[i]);
		return ret;
	}

	public int getNumRows() {
		return numRows;
	}

	public int getNumColumns() {
		return numColumns;
	}


	public float get(int rowIndex, int columnIndex) {
		int targetMatrix = -1;
		for (; (targetMatrix+1)<matrices.length-1; targetMatrix++) {
			if (columnIndex <= columnSplitPoints[targetMatrix+1])
				break;
		}
		targetMatrix++;
		int columnOffset=0;
		if (targetMatrix>0)
			columnOffset=columnSplitPoints[targetMatrix-1]+1;
		return matrices[targetMatrix].get(rowIndex, columnIndex-columnOffset);
	}

	public void set(int rowIndex, int columnIndex, float value) {
		int targetMatrix = -1;
		for (; (targetMatrix+1)<matrices.length-1; targetMatrix++) {
			if (columnIndex <= columnSplitPoints[targetMatrix+1])
				break;
		}
		targetMatrix++;
		int columnOffset=0;
		if (targetMatrix>0)
			columnOffset=columnSplitPoints[targetMatrix-1]+1;
		matrices[targetMatrix].set(rowIndex, columnIndex-columnOffset, value);
		//		System.err.println("not implemented!");
	}

}
