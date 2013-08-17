package de.ismll.table.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import de.ismll.table.IntMatrix;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.MatrixCallback;

/**
 * A generic (int) matrix interface.
 * 
 * @author Lars Schmidt-Thieme, Krisztian Buza
 * @version 1.0 [andre]
 */
public class DefaultIntMatrix implements IntMatrix {

	private static final class MatrixCallbackImplementation implements
	MatrixCallback {
		public DefaultIntMatrix matrix;

		public void setField(int row, int col, String string) {
			matrix.set(row, col, Integer.parseInt(string));
		}

		public void meta(int numRows, int numColumns) {
			matrix = new DefaultIntMatrix(numRows, numColumns);
		}
	}


	private static boolean debug;
	protected int numRows;
	protected int numColumns;
	public int[][] data;

	public DefaultIntMatrix(int numRows, int numColumns) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		data = new int[numRows][numColumns];
	}

	public static DefaultIntMatrix createMatrix(int[][] a) {
		int numRows = a.length;
		if (numRows>0) {
			int numColumns = a[0].length;
			DefaultIntMatrix d = new DefaultIntMatrix(numRows, numColumns);
			for (int i=0;i<numRows;i++)
				for (int j=0;j<numColumns;j++)
					d.set(i,j,a[i][j]);
			return d;
		}
		return new DefaultIntMatrix(0,0);
	}


	/**
	 * Wraps an array into a matrix.
	 */
	 public static DefaultIntMatrix asMatrix(int[][] array) {
		 return new DefaultIntMatrix(array);
	 }




	 /**
	  * copies values
	  * 
	  * @param a
	  * @return
	  */
	 public static DefaultIntMatrix createMatrix(IntMatrix a) {
		 int numRows = a.getNumRows();
		 int numColumns = a.getNumColumns();
		 DefaultIntMatrix d = new DefaultIntMatrix(numRows, numColumns);
		 for (int i=0;i<numRows;i++)
			 for (int j=0;j<numColumns;j++)
				 d.set(i,j,a.get(i,j));
		 return d;
	 }


	 protected DefaultIntMatrix(int[][] data) {
		 this.numRows = data.length;
		 if (numRows == 0)
			 this.numColumns = 0;
		 else
			 this.numColumns = data[0].length;
		 this.data = data;
	 }


	 /**
	  * Get the number of rows.
	  */
	 public int getNumRows() { return numRows;}

	 /**
	  * Get the number of columns.
	  */
	 public int getNumColumns() { return numColumns;}


	 // ----------------------------------------------------------------------
	 // Cell contents:

	 /**
	  * Get the value of a cell.
	  */
	 public int get(int rowIndex, int columnIndex) { return data[rowIndex][columnIndex]; }

	 /**
	  * Set the value of a cell.
	  */
	 public void set(int rowIndex, int columnIndex, int value) { data[rowIndex][columnIndex] = value; }

	 @Deprecated
	 public void set(int rowIndex, int columnIndex, String value) throws NumberFormatException { data[rowIndex][columnIndex] = Integer.parseInt(value); }



	 // ----------------------------------------------------------------------
	 // Elementary maths:

	 /**
	  * Add a value to a cell.
	  */
	 @Deprecated public void add(int rowIndex, int columnIndex, float value) { data[rowIndex][columnIndex] += value; }

	 /**
	  * Multiply a column by a scalar.
	  */
	 @Deprecated public void multiplyColumn(int colIndex, float scalar) {
		 for (int i = 0; i < numRows; ++i)
			 data[i][colIndex] *= scalar;
	 }

	 /**
	  * Multiply the matrix by a scalar.
	  */
	 @Deprecated public void mul(float scalar) {
		 for (int i = 0; i < numRows; ++i)
			 for (int j = 0; j < numColumns; ++j)
				 data[i][j] *= scalar;
	 }

	 /**
	  * Substracts a scalar.
	  */
	 @Deprecated public void sub(float scalar) {
		 for (int i = 0; i < numRows; ++i)
			 for (int j = 0; j < numColumns; ++j)
				 data[i][j] -= scalar;
	 }

	 /**
	  * Substracts a scalar.
	  */
	 @Deprecated public void subByCol(float[] scalar) {
		 for (int i = 0; i < numRows; ++i)
			 for (int j = 0; j < numColumns; ++j)
				 data[i][j] -= scalar[j];
	 }


	 /**
	  * Multiply a row of this matrix with a column of another matrix.
	  */
	 @Deprecated public float multiplyRowColumn(int rowIndex, Matrix other, int columnIndex) {
		 float val = 0;
		 for (int i = 0; i < numColumns; ++i)
			 val += data[rowIndex][i] * other.get(i, columnIndex);
		 return val;
	 }

	 @Deprecated public void multiplyRow(int rowIndex, float scalar) {
		 for (int i = 0; i < numColumns; ++i)
			 data[rowIndex][i] *= scalar;
	 }

	 /**
	  * Add a row to a (dense) column of another matrix.
	  */
	 @Deprecated public void addRowToOtherColumn(int rowIndex, float factor, DefaultIntMatrix other, int colIndex) {
		 for (int i = 0; i < numColumns; ++i) {
			 float val = data[rowIndex][i];
			 other.add(i, colIndex, factor * val);
		 }
	 }

	 // ----------------------------------------------------------------------
	 // IO:


	 @Deprecated public void write(String fn, int showProgress) throws IOException {
		 System.out.print("write matrix "+ fn + ": " + numRows + " x " + numColumns +"... ");
		 BufferedWriter out = new BufferedWriter(new FileWriter(fn));

		 out.write(numRows+","+numColumns+" \n");
		 for (int i = 0; i < numRows; ++i) {
			 if ( (i+1) % showProgress == 0)
				 System.out.print(".");
			 for (int j = 0; j < numColumns; ++j)
				 out.write(this.get(i, j)+((j!=numColumns-1)?",":"\n"));
		 }
		 out.close();
		 System.out.println("");
	 }

	 /** Assumes the last column to be the class value */
	 @Deprecated  public void writeAsLibLinear(String fn) throws IOException {
		 BufferedWriter out = new BufferedWriter(new FileWriter(fn));
		 for (int i = 0; i < numRows; ++i) {
			 out.write(this.get(i,numColumns-1)+"");
			 for (int j = 0; j < numColumns-1; ++j)
				 out.write(" "+(j+1)+":"+this.get(i, j)) ;
			 out.write("\n");
		 }
		 out.close();
	 }




	 @Deprecated public void writeAsArff(String fn, String cdomain, int showProgress) throws IOException {
		 //System.out.print("write matrix "+ fn + ": " + numRows + " x " + numColumns +"... ");
		 BufferedWriter out = new BufferedWriter(new FileWriter(fn));
		 out.write("@RELATION matrix \n");
		 for (int i=0;i<numColumns-1;i++)
			 out.write("@ATTRIBUTE attr"+i+" NUMERIC \n");

		 out.write("@ATTRIBUTE class "+cdomain+" \n");

		 out.write("@DATA \n");
		 for (int i = 0; i < numRows; ++i) {
			 if ( (i+1) % showProgress == 0)
				 System.out.print(".");
			 for (int j = 0; j < numColumns; ++j)
				 out.write(this.get(i, j)+((j!=numColumns-1)?",":"\n"));
		 }
		 out.close();
		 //System.out.println("");
	 }


	 @Deprecated public weka.core.Instances wekaInstances_io(String cdomain) throws IOException {
		 String filename = "weka_instances_tmp_file"+(Math.random()*10000000)+".arff";
		 writeAsArff(filename, cdomain, Integer.MAX_VALUE );
		 return new weka.core.Instances(new BufferedReader(new FileReader(new File(filename))));
	 }

	 @Deprecated public weka.core.Instances wekaInstances(String cdomain) throws IOException {
		 //System.out.print("write matrix "+ fn + ": " + numRows + " x " + numColumns +"... ");

		 class MyWriter extends Reader {
			 java.util.ArrayList<Character> buffer = new java.util.ArrayList<Character>();

			 public void write(String s) {
				 for (int i=0;i<s.length();i++)
					 buffer.add(s.charAt(i));
			 }
			 private int where_to_reset = 0;
			 private int current_position = 0;
			 public void close() {}
			 public void mark() { where_to_reset = current_position; }
			 public boolean markSupported() { return true; }
			 public int read() {
				 if (current_position<buffer.size()) {
					 int c = buffer.get(current_position);
					 current_position++;
					 return c;
				 } else return -1;
			 }
			 public int read(char[] cbuf, int off, int len) {
				 int n = 0;
				 if (current_position >= buffer.size()) return -1;
				 while ( (n<len) && (current_position < buffer.size()) ) {
					 cbuf[off+n] = buffer.get(current_position);
					 current_position++;
					 n++;
				 }
				 return n;
			 }
			 public int read(char[] cbuf) {
				 return read(cbuf, 0, cbuf.length);
			 }
			 public boolean ready() {
				 return true;
			 }
			 public void reset() {
				 current_position = where_to_reset;
			 }
			 public long skip(long n) {
				 current_position = current_position + (int) n;
				 return n;
			 }
		 }

		 MyWriter out = new MyWriter();
		 out.write("@RELATION matrix \n");
		 for (int i=0;i<numColumns-1;i++)
			 out.write("@ATTRIBUTE attr"+i+" NUMERIC \n");

		 out.write("@ATTRIBUTE class "+cdomain+" \n");

		 out.write("@DATA \n");
		 for (int i = 0; i < numRows; ++i) {
			 for (int j = 0; j < numColumns; ++j)
				 out.write(this.get(i, j)+((j!=numColumns-1)?",":"\n"));
		 }
		 out.close();

		 return new weka.core.Instances(out);
		 //System.out.println("");
	 }

	 @Deprecated public static DefaultIntMatrix read(File fn) throws IOException { return read(fn, 500000); }

	 @Deprecated public static DefaultIntMatrix read(File fn, int showProgress) throws IOException {
		 MatrixCallbackImplementation matrixCallbackImplementation = new MatrixCallbackImplementation();
		 Matrices.readDense(fn, showProgress, matrixCallbackImplementation);
		 return matrixCallbackImplementation.matrix;
	 }

	 @Deprecated  public static DefaultIntMatrix readFromSparse(String fn) throws IOException {
		 MatrixCallbackImplementation m = new MatrixCallbackImplementation();
		 Matrices.readSparse(new File(fn), m);
		 return m.matrix;
	 }

	 @Deprecated public DefaultIntVector getRowCopy(int row) {
		 DefaultIntVector d = new DefaultIntVector(this.getNumColumns());
		 for (int i=0;i<getNumColumns();i++)
			 d.set(i, this.get(row, i));
		 return d;
	 }

	 @Deprecated public IntVector getColumnCopy(int col) {
		 DefaultIntVector d = new DefaultIntVector(this.getNumRows());
		 for (int i=0;i<getNumRows();i++)
			 d.set(i,this.get(i, col));
		 return d;
	 }

	 @Deprecated  public int min() {
		 int min = this.get(0,0);
		 for (int i=0;i<getNumRows();i++)
			 for (int j=0;j<getNumColumns();j++)
				 if (get(i,j)<min) min=get(i,j);
		 return min;
	 }

	 @Deprecated public int max() {
		 int max = this.get(0,0);
		 for (int i=0;i<getNumRows();i++)
			 for (int j=0;j<getNumColumns();j++)
				 if (get(i,j)>max) max=get(i,j);
		 return max;
	 }


	 @Deprecated public float avgRow(int row, DefaultIntMatrix weights) {
		 float a = 0, c = 0;
		 for (int i=0;i<getNumColumns();i++) {
			 a+=(get(row, i)*weights.get(0, i));
			 c+=weights.get(0,i);
		 }
		 return ( a/c );
	 }


	 public String toString() {
		 return Matrices.toString(this);
	 }

	 @Deprecated public Integer getExpensive(int row, int col) {
		 return Integer.valueOf(get(row, col));
	 }
}