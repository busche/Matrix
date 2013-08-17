/**
 * DefaultVector.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.table.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import de.ismll.table.IntVector;
import de.ismll.table.Vector;

/**
 * A simplistic (float) vector.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public class DefaultVector implements Vector {

	public float[] data;

	public DefaultVector(int size) {
		this(new float[size], false);
	}

	/**
	 * Copies the contents of the argument vector.
	 */
	public DefaultVector(Vector x) {
		data = new float[x.size()];
		for (int i = 0; i < x.size(); ++i)
			data[i] = x.get(i);
	}

	/**
	 * Copies the contents of the argument vector.
	 */
	public DefaultVector(float[] x) {
		this(x, true);
	}

	public DefaultVector(IntVector vec) {
		data = new float[vec.size()];
		for (int i = 0; i < vec.size(); ++i)
			data[i] = vec.get(i);
	}

	private DefaultVector(float[] data2, boolean copy) {
		if (copy) {
			data = new float[data2.length];
			for (int i = 0; i < data2.length; ++i)
				this.data[i] = data2[i];
		} else {
			this.data=data2;
		}
	}


	public static DefaultVector wrap(float[] data) {
		return new DefaultVector(data, false);
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
	 public float get(int index) { return data[index]; }

	 /**
	  * Set the value of a cell.
	  */
	 public void set(final int index, final float value) { data[index] = value; }


	 // ----------------------------------------------------------------------
	 // IO:

	 @Deprecated  public static DefaultVector read(File fn) throws IOException {
		 // TODO: move to other place and add autosensing capability for other Vector file formats.
		 BufferedReader in = new BufferedReader(new FileReader(fn));
		 Scanner sc = new Scanner(in);
		 int size = sc.nextInt();
		 DefaultVector vector = new DefaultVector(size);
		 for (int i = 0; i < size; ++i)
			 vector.set(i, sc.nextFloat());
		 sc.close();
		 in.close();
		 return vector;
	 }



	 public static Vector wrap(int[] data) {
		 float[] data2 = new float[data.length];
		 for (int i = 0; i < data.length ; i++) {
			 data2[i] = data[i];
		 }
		 return wrap(data2);
	 }



}