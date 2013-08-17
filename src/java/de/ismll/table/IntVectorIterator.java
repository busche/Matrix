package de.ismll.table;

import java.util.Iterator;

public class IntVectorIterator implements Iterator<Integer> {

	private final IntVector inputVector;

	int currentPosition = 0;

	public IntVectorIterator(IntVector inputVector) {
		this.inputVector = inputVector;
	}

	@Override
	public boolean hasNext() {
		return currentPosition < inputVector.size();
	}

	@Override
	public Integer next() {
		return Integer.valueOf(inputVector.get(currentPosition++));
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
