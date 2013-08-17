package de.ismll.table.projections;

import de.ismll.table.IntVector;
import de.ismll.table.Vector;

public class IntVectorView implements IntVector {

	private Vector m;

	public IntVectorView(Vector v) {
		this.m = v;
	}

	@Override
	public int size() {
		return m.size();
	}


	@Override
	public int get(int index) {
		return (int) m.get(index);
	}


	@Override
	public void set(int index, int value) {
		m.set(index, value);
	}

}
