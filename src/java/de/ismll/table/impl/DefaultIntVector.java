/**
 * DefaultIntVector.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.table.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.ismll.table.BitVector;
import de.ismll.table.IntVector;
import de.ismll.table.Vector;
import de.ismll.table.VectorCallback;
import de.ismll.table.Vectors;

/**
 * A simplistic (int) vector.
 * 
 * @author Lars Schmidt-Thieme, Andre Busche
 * @version 1.1
 */
public class DefaultIntVector implements IntVector {

	private static final class VectorCallbackImplementation implements
	VectorCallback {
		public DefaultIntVector vector;

		public VectorCallbackImplementation() {
			super();
		}

		public void setField(int position, String string) {
			this.vector.set(position, Integer.parseInt(string));
		}

		public void meta(int numElements) {
			this.vector = new DefaultIntVector(numElements);
		}
	}


	public final int[] data;

	public DefaultIntVector(int size) {
		data = new int[size];
	}

	/**
	 * Copies the contents of the argument vector.
	 */
	public DefaultIntVector(IntVector x) {
		data = new int[x.size()];
		for (int i = 0; i < x.size(); ++i)
			data[i] = x.get(i);
	}

	public DefaultIntVector(int[] data) {
		this(data, true);
	}

	private DefaultIntVector(int[] data, boolean copy) {
		super();
		if (copy) {
			this.data = new int[data.length];
			for (int i = 0; i < data.length; ++i)
				this.data[i] = data[i];
		} else {
			this.data = data;
		}
	}

	public DefaultIntVector(Vector v) {
		this(extract(v));
	}

	private static IntVector extract(Vector v) {
		DefaultIntVector ret= new DefaultIntVector(v.size());
		for (int i = 0; i < v.size(); i++)
			ret.set(i, (int) v.get(i));
		return ret;
	}

	public static DefaultIntVector wrap(int[] data){
		return new DefaultIntVector(data, false);
	}
	/**
	 * Get the size.
	 */
	 public int size() { return data.length; }


	 // ----------------------------------------------------------------------
	 // Cell contents:

	 /**
	  * Get the value of a cell.
	  */
	 public int get(int index) { return data[index]; }

	 /**
	  * Set the value of a cell.
	  */
	 public void set(int index, int value) { data[index] = value; }


	 // ----------------------------------------------------------------------
	 // IO:

	 @Deprecated public static DefaultIntVector readSlow(File fn) throws IOException {
		 VectorCallbackImplementation i = new VectorCallbackImplementation();
		 Vectors.readDenseSlow(fn, i);
		 return i.vector;
	 }

	 public BitVector asBitvector(int splitValue) {
		 BitVector ret = new DefaultBitVector(size());
		 int idx = 0;
		 for (int i = 0; i < size(); i++)
			 ret.set(idx++, data[i]==splitValue);
		 return ret;

	 }

	 @Deprecated public static DefaultIntVector read(File fn) throws FileNotFoundException {
		 VectorCallbackImplementation i = new VectorCallbackImplementation();
		 Vectors.readDense(fn, 500000, i);
		 return i.vector;
	 }

	 public String toString() {
		 return Vectors.toString(this);
	 }

}