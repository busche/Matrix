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
public class DefaultMatrix implements Matrix {


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + numColumns;
		result = prime * result + numRows;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultMatrix other = (DefaultMatrix) obj;
		if (!Arrays.deepEquals(data, other.data))
			return false;
		if (numColumns != other.numColumns)
			return false;
		if (numRows != other.numRows)
			return false;
		return true;
	}

	public static final class DefaultMatrixParser implements MatrixCallback {

		public DefaultMatrix m ;

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
			m= new DefaultMatrix(numRows, numColumns);
		}
	}

	@Deprecated public static boolean debug;
	protected final int numRows;
	protected final int numColumns;
	public final float[][] data;

	public DefaultMatrix(int numRows, int numColumns) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		data = new float[numRows][numColumns];
	}

	/**
	 * wraps!
	 * 
	 * @param data
	 */
	protected DefaultMatrix(float[][] data) {
		this.numRows = data.length;
		if (numRows == 0)
			this.numColumns = 0;
		else
			this.numColumns = data[0].length;
		this.data = data;
	}

	/**
	 * copy-constructor
	 * 
	 * @param copyFromOther
	 */
	public DefaultMatrix(Matrix copyFromOther) {
		this.numRows = copyFromOther.getNumRows();
		this.numColumns = copyFromOther.getNumColumns();

		data = new float[numRows][numColumns];
		for (int r = 0; r < numRows; r++)
			for (int c = 0; c < numColumns; c++)
				data[r][c] = copyFromOther.get(r, c);
	}

	public static DefaultMatrix createMatrix(double[][] a) {
		int numRows = a.length;
		if (numRows>0) {
			int numColumns = a[0].length;
			DefaultMatrix d = new DefaultMatrix(numRows, numColumns);
			for (int i=0;i<numRows;i++)
				for (int j=0;j<numColumns;j++)
					d.set(i,j,(float) a[i][j]);
			return d;
		}

		return new DefaultMatrix(0,0);
	}



	/**
	 * Wraps an array into a matrix.
	 */
	public static DefaultMatrix wrap(float[][] array) {
		return new DefaultMatrix(array);
	}

	/**
	 * copies values
	 * 
	 * @param a
	 * @return
	 */
	public static DefaultMatrix createMatrix(Matrix a) {
		int numRows = a.getNumRows();
		int numColumns = a.getNumColumns();
		DefaultMatrix d = new DefaultMatrix(numRows, numColumns);
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
	public final float get(final int rowIndex, final int columnIndex) { return data[rowIndex][columnIndex]; }

	/**
	 * Set the value of a cell.
	 */
	public final void set(final int rowIndex, final int columnIndex, final float value) { data[rowIndex][columnIndex] = value; }



	// ----------------------------------------------------------------------
	// Elementary maths:

	/**
	 * Add a value to a cell.
	 */
	@Deprecated public void add(int rowIndex, int columnIndex, float value) { data[rowIndex][columnIndex] += value; }







	// ----------------------------------------------------------------------
	// IO:


	/**
	 * use Matrices.write instead!
	 * 
	 * @param fn
	 * @param showProgress
	 * @throws IOException
	 * 
	 */
	@Deprecated
	public void write(String fn, int showProgress) throws IOException {
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
	@Deprecated public void writeAsLibLinear(String fn) throws IOException {
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
		writeAsArff(new File(fn), cdomain, showProgress);
	}

	/**
	 * @param fn the target file (name)
	 * @param cdomain the domain identifier for the last column (assumed to be the class), e.g. NUMERIC, or a WEKA nominal String.
	 * @param showProgress counter on command line
	 * @throws IOException on error
	 */
	@Deprecated public void writeAsArff(File fn, String cdomain, int showProgress) throws IOException {
		Matrices.writeArff(fn, this, cdomain, showProgress);
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
//			public void mark() { where_to_reset = current_position; }
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

	@Deprecated public static DefaultMatrix read(File fn) throws IOException { return read(fn, 500000); }



	@Deprecated public static DefaultMatrix readWithReader(BufferedReader r, int showProgress) throws IOException {
		long start = System.nanoTime();
		//		BufferedReader r = new BufferedReader(new FileReader(fn), LineawareLongScanner.DEFAULT_BUFFER_SIZE);
		String line0 = r.readLine().trim();
		String[] line_arr;
		if (line0.contains("\t"))
			line_arr= line0.split("\t");
		else
			line_arr= line0.split(",");
		int numRows = Integer.parseInt(line_arr[0].trim());
		int numColumns = Integer.parseInt(line_arr[1].trim());
		DefaultMatrix matrix = new DefaultMatrix(numRows, numColumns);
		for (int row = 0; row<numRows; row++) {
			String lne = r.readLine();
			if (lne == null) {
				System.err.println("invalid matrix file: row " + row + " actually not present.");
				continue;
			}
			String[] line = lne.trim().split(",");
			for (int j = 0; j < numColumns; ++j)
				matrix.set(row, j, Float.parseFloat( line[j].trim() ));
		}
		r.close();

		long end = System.nanoTime();
		if (debug)

			System.out.println("Loaded with reader in " + (end-start) + " nanoseconds.");
		return matrix;
	}


	@Deprecated public static DefaultMatrix readWithFileChannel(FileChannel newChannel, int showProgress) throws IOException {
		long start = System.nanoTime();

		StringBuilder sb = new StringBuilder();

		int numRows, numColumns = -1;

		boolean headerRead= false;
		DefaultMatrix matrix = null;
		int colNr = 0;
		int rowNr = 0;

		boolean previousLinebreak=false;

		MappedByteBuffer mb =  newChannel.map( MapMode.READ_ONLY,
				0L,  newChannel.size( ) );

		while(mb.hasRemaining()){

			byte b = mb.get();
			boolean lineBreak = b == '\n' || b == '\r';

			if (!headerRead) {
				// seek for numCols and numRows
				sb.append((char)b);

				if (lineBreak) {
					headerRead=true;

					String line0 = sb.toString().trim();
					String[] line_arr;
					if (line0.contains("\t"))
						line_arr= line0.split("\t");
					else
						line_arr= line0.split(",");
					numRows = Integer.parseInt(line_arr[0].trim());
					numColumns = Integer.parseInt(line_arr[1].trim());
					matrix = new DefaultMatrix(numRows, numColumns);
					sb.setLength(0);
				}
			} else {
				// header read; parse lines...

				if (b==',') {
					// parse value
					matrix.set(rowNr, colNr, Float.parseFloat(sb.toString()));
					colNr++;
					sb.setLength(0);
				}
				else if (lineBreak && !previousLinebreak) {
					// parse value
					matrix.set(rowNr, colNr, Float.parseFloat(sb.toString()));
					colNr++;
					sb.setLength(0);
				}
				else {
					// trim whitespace
					if (b != ' ' && !lineBreak)
						sb.append((char)b);
				}

				if (lineBreak && colNr>0)
					rowNr++;
				if (lineBreak)
					colNr = 0;
			}
			previousLinebreak=lineBreak;

		}

		long end = System.nanoTime();
		if (debug)

			System.out.println("Loaded from Channel                                                                 in " + (end-start) + " nanoseconds.");
		newChannel.close();
		return matrix;
	}

	@Deprecated public static DefaultMatrix read(File fn, int showProgress) throws IOException {

		DefaultMatrixParser mc = new DefaultMatrixParser();
		Matrices.readDense(fn, showProgress, mc);
		return mc.m;
	}

	@Deprecated public static DefaultMatrix readDirect(String fn, int showProgress) throws IOException {
		long start = System.nanoTime();

		//    	LineawareLongScanner.DEFAULT_BUFFER_SIZE=LineawareLongScanner.determineBufferSize(new File(fn).toURL());
		BufferedInputStream fis = new BufferedInputStream(new FileInputStream(fn));

		byte[] buffer = new byte[8192];
		StringBuilder sb = new StringBuilder();

		int numRows, numColumns = -1;

		int read;
		boolean headerRead= false;
		DefaultMatrix matrix = null;
		int colNr = 0;
		int rowNr = 0;

		boolean previousLinebreak=false;

		while((read = fis.read(buffer))>0 ) {

			for (int i = 0; i < read; i++) {
				byte b = buffer[i];
				boolean lineBreak = b == '\n' || b == '\r';

				if (!headerRead) {
					// seek for numCols and numRows
					sb.append((char)b);

					if (lineBreak) {
						headerRead=true;

						String line0 = sb.toString().trim();
						String[] line_arr;
						if (line0.contains("\t"))
							line_arr= line0.split("\t");
						else
							line_arr= line0.split(",");
						numRows = Integer.parseInt(line_arr[0].trim());
						numColumns = Integer.parseInt(line_arr[1].trim());
						matrix = new DefaultMatrix(numRows, numColumns);
						sb.setLength(0);
					}
				} else {
					// header read; parse lines...

					if (b==',') {
						// parse value
						matrix.set(rowNr, colNr, Float.parseFloat(sb.toString()));
						colNr++;
						sb.setLength(0);
					}
					else if (lineBreak && !previousLinebreak) {
						// parse value
						matrix.set(rowNr, colNr, Float.parseFloat(sb.toString()));
						colNr++;
						sb.setLength(0);
					}
					else {
						// trim whitespace
						if (b != ' ' && !lineBreak)
							sb.append((char)b);
					}

					if (lineBreak && colNr>0) {
						rowNr++;
						if (rowNr>0 && rowNr % showProgress == 0)
							System.out.println(rowNr);

					}
					if (lineBreak)
						colNr = 0;
				}
				previousLinebreak=lineBreak;


			}
		}

		fis.close();
		long end = System.nanoTime();
		if (debug)
			System.out.println("Loaded " + fn + " direct      in " + (end-start) + " nanoseconds.");
		return matrix;
	}

	@Deprecated public static DefaultMatrix readFromSparse(String fn) throws FileNotFoundException  {
		return readFromSparse(new File(fn));
	}

	@Deprecated public static DefaultMatrix readFromSparse(File fn) throws FileNotFoundException  {
		DefaultMatrixParser m = new DefaultMatrixParser();
		Matrices.readSparse(fn, m);
		return m.m;
	}



	@Deprecated public void setCol(int col, Vector autoCorrelate) {
		for (int i = 0; i < autoCorrelate.size(); i++) set(i, col, autoCorrelate.get(i));
	}





	public String toString() {
		return Matrices.toString(this);
	}

	public static DefaultMatrix convert(Object o) throws IOException {
		File src;
		if (o instanceof File)
			src = (File) o;
		else {
			String string = o.toString();
			src = new File(string);
		}
		return read(src);


	}



}