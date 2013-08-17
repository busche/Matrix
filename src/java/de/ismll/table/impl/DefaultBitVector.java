package de.ismll.table.impl;

import java.io.File;
import java.io.FileNotFoundException;

import de.ismll.table.BitVector;
import de.ismll.table.VectorCallback;
import de.ismll.table.Vectors;

public class DefaultBitVector implements BitVector {

	private static final class VectorCallbackImplementation implements
	VectorCallback {
		public DefaultBitVector vector;

		public VectorCallbackImplementation() {
			super();
		}

		public void setField(int position, String string) {
			boolean parsed = false;
			if ("0".equals(string)) {
				vector.data[position]=false;
				parsed=true;
			}
			else if ("1".equals(string)) {
				vector.data[position]=true;
				parsed=true;
			}
			if (!parsed)
				vector.data[position]=Boolean.parseBoolean(string);

		}

		public void meta(int numElements) {
			vector=new DefaultBitVector(numElements);
		}
	}

	private boolean[] data;

	public DefaultBitVector(int numElements) {
		super();
		this.data = new boolean[numElements];
	}

	private DefaultBitVector(boolean[] data2, boolean copy) {
		if (copy) {
			this.data = new boolean[data2.length];
			for (int i = 0; i < data2.length; i++)
				this.data[i] = data2[i];
		} else {
			this.data=data2;
		}
	}

	public static DefaultBitVector wrap(boolean[] data) {
		return new DefaultBitVector(data, false);
	}


	public int size() {
		return data.length;
	}

	public boolean get(int position) {
		return data[position];
	}


	public static DefaultBitVector read(File f) throws FileNotFoundException {
		VectorCallbackImplementation i =new VectorCallbackImplementation();
		Vectors.readDense(f, 500000, i);
		return i.vector;

	}

	public void set(int position, boolean value) {
		data[position]=value;
	}
}
