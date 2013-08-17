package de.ismll.table.projections;

import de.ismll.table.Matrix;

public class RowUnionMatrixView implements Matrix{

	private final Matrix[] matrices;
	private int numRows;
	private int[] rowSplitPoints;
	private int numColumns;

	public RowUnionMatrixView(Matrix[] matrices) {
		super();
		this.matrices = matrices;
		if (matrices.length<1)
			throw new IllegalArgumentException("need at least one matrix!");
		numColumns = matrices[0].getNumColumns();
		rowSplitPoints = new int[matrices.length-1];
		int delta=0;

		for (int i = 1; i < matrices.length; i++){
			rowSplitPoints[i-1] = delta + matrices[i-1].getNumRows()-1;
			delta += matrices[i-1].getNumRows();

			if (numColumns != matrices[i].getNumColumns())
				throw new IllegalArgumentException("Rows mismatch. Matrices need to have the same amount of columns (" + numColumns  + "!=" + matrices[i].getNumColumns() + ")!");
		}
		this.numRows = matrices[matrices.length-1].getNumRows()+delta;
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
			if (rowIndex <= rowSplitPoints[targetMatrix+1])
				break;
		}
		targetMatrix++;
		int rowOffset=0;
		if (targetMatrix>0)
			rowOffset=rowSplitPoints[targetMatrix-1]+1;
		return matrices[targetMatrix].get(rowIndex-rowOffset, columnIndex);
	}

	public void set(int rowIndex, int columnIndex, float value) {
		int targetMatrix = -1;
		for (; (targetMatrix+1)<matrices.length-1; targetMatrix++) {
			if (rowIndex <= rowSplitPoints[targetMatrix+1])
				break;
		}
		targetMatrix++;
		int rowOffset=0;
		if (targetMatrix>0)
			rowOffset=rowSplitPoints[targetMatrix-1]+1;
		matrices[targetMatrix].set(rowIndex-rowOffset, columnIndex, value);
	}

}
