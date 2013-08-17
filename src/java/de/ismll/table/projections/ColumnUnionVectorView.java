package de.ismll.table.projections;

import de.ismll.table.Vector;
import de.ismll.table.Vectors;

/**
 * Stacking of columns
 * 
 * @author John
 *
 */
public class ColumnUnionVectorView implements Vector{

	private final Vector[] vectors;
	private int numRows;
	private int[] rowSplitPoints;

	public ColumnUnionVectorView(Vector[] vectors) {
		super();
		this.vectors = vectors;
		if (vectors.length<1)
			throw new IllegalArgumentException("need at lease one vector!");
		rowSplitPoints = new int[vectors.length-1];
		int delta=0;
		numRows=0;
		for (int i = 1; i < vectors.length; i++){
			rowSplitPoints[i-1] = delta + vectors[i-1].size()-1;
			delta += vectors[i-1].size();
			numRows += vectors[i-1].size();
		}
		numRows += vectors[vectors.length-1].size();
	}

	public float get(int rowIndex) {
		int targetVector = -1;
		int sub = 0;
		for (; (targetVector+1)<vectors.length-1; targetVector++) {
			if (rowIndex <= rowSplitPoints[targetVector+1])
				break;
			sub = rowSplitPoints[targetVector+1]+1;
		}
		targetVector++;
		return vectors[targetVector].get(rowIndex-sub);
	}

	@Override
	public int size() {
		return numRows;
	}

	@Override
	public void set(int rowIndex, float value) {
		int targetVector = -1;
		int sub = 0;
		for (; (targetVector+1)<vectors.length-1; targetVector++) {
			if (rowIndex <= rowSplitPoints[targetVector+1])
				break;
			sub = rowSplitPoints[targetVector+1]+1;
		}
		targetVector++;
		vectors[targetVector].set(rowIndex-sub, value);
	}

	public String toString() {
		return Vectors.toString(this);
	}
}
