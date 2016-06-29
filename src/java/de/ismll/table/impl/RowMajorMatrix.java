/**
 * DefaultMatrix.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.table.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.MatrixCallback;
import de.ismll.table.Vector;

/**
 * A generic (float) matrix interface.
 * 
 * Modified for Hough transformation. Added:
 *  - method to save the matrix
 *  - create method based on float[][], long[][], ...
 * 
 * @author Lars Schmidt-Thieme, Krisztian Buza, Andre Busche
 * @version 1.1 [krisztian]
 * @version 1.2 [andre]
 */
public class RowMajorMatrix implements Matrix {


	public static final class DefaultMatrixParser implements MatrixCallback {

		public RowMajorMatrix m ;

		public DefaultMatrixParser() {
			super();
		}

		public void setField(int row, int col, String string) {
			if (string.contains(".")) {
				m.set(row, col, Float.parseFloat(string));
			}
			else
				m.set(row, col, Integer.parseInt(string));
		}

		public void meta(int numRows, int numColumns) {
			m= new RowMajorMatrix(numRows, numColumns);
		}
	}

	protected final int numRows;
	protected final int numColumns;
	public final float[] data;

	public RowMajorMatrix(int numRows, int numColumns) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		data = new float[numRows*numColumns];
	}

	/**
	 * wraps!
	 * 
	 * @param data
	 */
	protected RowMajorMatrix(float[] data, int numCols) {
		this.numColumns = numCols;
		this.numRows = data.length / numCols;
		if (data.length % numCols != 0) {
			System.err.println("Warning: last row is incomplete - likely results in an Exception soon ...");
		}
		this.data = data;
	}

	/**
	 * copy-constructor
	 * 
	 */
	public RowMajorMatrix(Matrix copyFromOther) {
		this.numRows = copyFromOther.getNumRows();
		this.numColumns = copyFromOther.getNumColumns();

		data = new float[numRows*numColumns];
		for (int r = 0; r < numRows; r++)
			for (int c = 0; c < numColumns; c++)
				set(r,c,copyFromOther.get(r, c));
	}

	public static RowMajorMatrix createMatrix(double[][] a) {
		int numRows = a.length;
		if (numRows>0) {
			int numColumns = a[0].length;
			RowMajorMatrix d = new RowMajorMatrix(numRows, numColumns);
			for (int i=0;i<numRows;i++)
				for (int j=0;j<numColumns;j++)
					d.set(i,j,(float) a[i][j]);
			return d;
		}

		return new RowMajorMatrix(0,0);
	}



	/**
	 * Wraps an array into a matrix.
	 */
	public static RowMajorMatrix wrap(float[] array, int numColumns) {
		return new RowMajorMatrix(array, numColumns);
	}

	/**
	 * copies values
	 * 
	 */
	public static RowMajorMatrix createMatrix(Matrix a) {
		int numRows = a.getNumRows();
		int numColumns = a.getNumColumns();
		RowMajorMatrix d = new RowMajorMatrix(numRows, numColumns);
		for (int i=0;i<numRows;i++)
			for (int j=0;j<numColumns;j++)
				d.set(i,j,a.get(i,j));
		return d;
	}



	/**
	 * Get the number of rows.
	 */
	public final int getNumRows() { return numRows;}

	/**
	 * Get the number of columns.
	 */
	public final int getNumColumns() { return numColumns;}


	// ----------------------------------------------------------------------
	// Cell contents:

	/**
	 * Get the value of a cell.
	 */
	public final float get(final int rowIndex, final int columnIndex) { return data[rowIndex*numColumns + columnIndex]; }

	/**
	 * Set the value of a cell.
	 */
	public final void set(final int rowIndex, final int columnIndex, final float value) { data[rowIndex*numColumns + columnIndex] = value; }





	public String toString() {
		return Matrices.toString(this);
	}

//	public static RowMajorMatrix convert(Object o) throws IOException {
//		File src;
//		if (o instanceof File)
//			src = (File) o;
//		else {
//			String string = o.toString();
//			src = new File(string);
//		}
//		return Matrices.read(src, null);
//
//
//	}



}