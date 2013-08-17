package de.ismll.table.projections;

import de.ismll.table.BitVector;
import de.ismll.table.IntVector;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultIntVector;


/**
 * TODO: Move to {@link RowSubsetVectorView} and therein target either a Matrix or a Vector
 * 
 * @author Andre Busche
 */
@Deprecated
public class RowSubsetVector implements Vector {

	private Vector target;
	private final IntVector pointers;

	public RowSubsetVector(Vector target, IntVector pointers) {
		super();
		this.target = target;
		this.pointers = pointers;
	}

	public RowSubsetVector(Vector target, int[] pointers) {
		this(target, new DefaultIntVector(pointers));
	}

	public RowSubsetVector(Vector target, BitVector mask) {
		this(target, Vectors.convert2Pointers(mask));
	}

	public float get(int index) {
		return target.get(pointers.get(index));
	}

	public void set(int index, float value) {
		target.set(pointers.get(index), value);
	}

	public int size() {
		return pointers.size();
	}

}
