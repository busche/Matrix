package de.ismll.table.projections;

import de.ismll.table.IntVector;
import de.ismll.table.Vector;

public class VectorSubset implements Vector{

	private final Vector target;
	private final IntVector pointers;

	public VectorSubset(Vector target, IntVector pointers) {
		super();
		this.target = target;
		this.pointers = pointers;
	}

	@Override
	public int size() {
		return pointers.size();
	}

	@Override
	public float get(int index) {
		return target.get(pointers.get(index));
	}

	@Override
	public void set(int index, float value) {
		target.set(pointers.get(index), value);
	}

}
