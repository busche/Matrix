/**
 * RowSubsetIntVector.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.table.projections;


import de.ismll.table.BitVector;
import de.ismll.table.IntVector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultIntVector;


/**
 * A simple view on a subset of the entries of a IntVector.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public class RowSubsetIntVector implements IntVector {

	protected IntVector vector;
	protected IntVector index;


	public RowSubsetIntVector(IntVector target, IntVector pointers) {
		super();
		this.vector = target;
		this.index = pointers;
	}

	public RowSubsetIntVector(IntVector vector, IntVector split, int splitValue) {
		this.vector = vector;
		this.index = Vectors.convert2Pointers(split, splitValue);
	}

	public RowSubsetIntVector(IntVector vector, BitVector split) {
		this.vector = vector;
		this.index = Vectors.convert2Pointers(split);
	}

	/**
	 * Get the size.
	 */
	public int size() { return index.size(); }

	// ----------------------------------------------------------------------
	// Cell contents:

	/**
	 * Get the value of a cell.
	 */
	public int get(int idx) { return vector.get(index.get(idx)); }

	/**
	 * Set the value of a cell.
	 */
	public void set(int idx, int value) { vector.set(index.get(idx), value); }

	public static RowSubsetIntVector create(IntVector data,
			IntVector withoutIndizes) {

		IntVector iv = new DefaultIntVector(data.size());
		for (int i = 0; i < iv.size(); i++)
			iv.set(i, i);
		DefaultIntVector cp = new DefaultIntVector(withoutIndizes);
		Vectors.sort(cp);
		IntVector mask = Vectors.removeAll(iv, cp);
		return new RowSubsetIntVector(data, mask);

	}

}
