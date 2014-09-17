/**
 * SparseRowMatrix.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.table.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.regex.Pattern;

import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;

/**
 * A simple matrix with sparse rows.
 * The sparsity structure cannot be changed after creation.
 * 
 * @author Lars Schmidt-Thieme, Andre Busche
 * @version 1.0
 * @version 1.1 [andre]
 */
public class SparseRowMatrix implements Matrix {

	protected int numRows;
	protected int numColumns;
	// before making these fields public, have a look at #getStoredColumns
	protected int[][] index;
	// before making these fields public, have a look at #getStoredValues
	protected float[][] value;

	public SparseRowMatrix(int numRows, int numColumns, int[][] index, float[][] value) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.index = index;
		this.value = value;
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
	// Cell contents (dense accessors):

	/**
	 * Get the value of a cell.
	 * This is an expensive procedure as the cell has to be looked up!
	 */
	public float get(int rowIndex, int columnIndex) {
		int idx = Arrays.binarySearch(index[rowIndex], columnIndex);
		return idx < 0 ? 0 : value[rowIndex][idx];
	}

	/**
	 * Set the value of a cell.
	 * This is an expensive procedure as the cell has to be looked up!
	 */
	public void set(int rowIndex, int columnIndex, float val) {
		int idx = Arrays.binarySearch(index[rowIndex], columnIndex);
		if (idx == -1 && val != 0) {
			System.err.println("ERROR: tried to set a non-stored cell.");
			System.exit(-1);
		} else
			value[rowIndex][idx] = val;
	}


	// ----------------------------------------------------------------------
	// Cell contents (sparse accessors):

	/**
	 * Get the stored columns in a row.
	 */
	public int[] getStoredColumns(int rowIndex) {
		return index[rowIndex];
	}

	/**
	 * Get the stored cell values in a row.
	 */
	public float[] getStoredValues(int rowIndex) {
		return value[rowIndex];
	}


	// ----------------------------------------------------------------------
	// Elementary maths:

	/**
	 * Add a value to a cell.
	 * This is an expensive procedure as the cell has to be looked up!
	 */
	public void add(int rowIndex, int columnIndex, float val) {
		int idx = Arrays.binarySearch(index[rowIndex], columnIndex);
		if (idx == -1 && val != 0) {
			System.err.println("ERROR: tried to add to a non-stored cell.");
			System.exit(-1);
		} else
			value[rowIndex][idx] += val;
	}

	/**
	 * Multiply a given row with a (dense) vector.
	 * Fast as it uses the sparsity.
	 */
	public float multiplyRow(int rowIndex, Vector x) {
		float result = 0;
		for (int i = 0; i < index[rowIndex].length; ++i) {
			int idx = index[rowIndex][i];
			result += x.get(idx) * value[rowIndex][i];
		}
		return result;
	}



	// ----------------------------------------------------------------------
	// MatrixOperations:

	/**
	 * Multiply a row of this matrix with a column of another matrix.
	 */
	public float multiplyRowColumn(int rowIndex, Matrix other, int columnIndex) {
		float val = 0;
		for (int i = 0; i < index[rowIndex].length; ++i)
			val += value[rowIndex][i] * other.get(index[rowIndex][i], columnIndex);
		return val;
	}

	/**
	 * Add a row to a (dense) column of another matrix.
	 * Fast as it uses the sparsity.
	 */
	public void addRowToOtherColumn(int rowIndex, float factor, DefaultMatrix other, int colIndex) {
		for (int i = 0; i < index[rowIndex].length; ++i) {
			int row = index[rowIndex][i];
			float val = value[rowIndex][i];
			other.add(row, colIndex, factor * val);
		}
	}


	// ----------------------------------------------------------------------
	// IO:

	public static void createFormatFileLibSVM(File fn) throws IOException {
		System.out.println("create format file for " + fn + " (1st pass)...");

		LineNumberReader read = new LineNumberReader(new FileReader(fn));
		int numRows = 0;
		int numCols = 0;
		String line=null;
		while ((line = read.readLine())!=null) {

			String[] lne = line.split(" ");
			numRows++;
			if (lne.length<2)
				continue;
			String[] last = lne[lne.length-1].split(":");
			int colIdx = Integer.parseInt(last[0]);

			if (colIdx > numCols)
				numCols = colIdx;
		}
		read.close();

		//    	++numRows;
		++numCols;
		System.out.println();
		System.out.println("detected sparse matrix " + numRows + " x " + numCols);

		// 2. pass: count #cells in each row:
		System.out.println("create format file for " + fn + " (2nd pass)...");
		int[] rowLength = new int[numRows];
		read = new LineNumberReader(new FileReader(fn));
		int lineNumber = 0;

		while ((line = read.readLine())!=null) {
			String[] lne=line.split(" ");

			rowLength[lineNumber] = lne.length;
			++lineNumber;
		}
		read.close();

		// 3. write the format file:
		BufferedWriter out = new BufferedWriter(new FileWriter(fn + ".format"));
		out.write(numRows + "\t" + numCols);
		out.newLine();
		out.write("" + rowLength[0]);
		for (int i = 1; i < numRows; ++i)
			out.write("\t" + rowLength[i]);
		out.newLine();
		out.close();
	}

	public static SparseRowMatrix readLibSVM(File fn, int showProgress) throws IOException {
		File formatFile = new File(fn.getAbsoluteFile() + ".format");
		if (!formatFile.exists()) {
			System.out.println("Format file does not exist ... creating...");
			createFormatFileLibSVM(fn);
		}
		Pattern _pt = Pattern.compile("\t");
		Pattern _pw = Pattern.compile(" ");
		Pattern _pc = Pattern.compile(":");

		LineNumberReader readFormat = new LineNumberReader(new FileReader(formatFile));

		String formatHeader = readFormat.readLine();
		String[] split = _pt.split(formatHeader);

		int numRows = Integer.parseInt(split[0]);
		int numColumns = Integer.parseInt(split[1]);

		String counts = readFormat.readLine();

		String[] countsSplit = _pt.split(counts);

		int[] rowLength = new int[numRows];
		int[][] index = new int[numRows][];
		float[][] value = new float[numRows][];
		for (int i = 0; i < numRows; ++i) {
			rowLength[i] = Integer.parseInt(countsSplit[i]);
			index[i] = new int[rowLength[i]];
			value[i] = new float[rowLength[i]];
		}
		readFormat.close();

		System.out.println("read sparse matrix " + numRows + " x " + numColumns);

		// 2. read the triples:
		LineNumberReader readData = new LineNumberReader(new FileReader(fn.getAbsoluteFile()));

		int row = 0;
		String line;
		while ((line = readData.readLine())!=null) {

			if (row > 0 && row % showProgress == 0) {
				System.out.print("\r " + row);
			}

			String[] splitData = _pw.split(line);
			int idx = 0;

			float classValue = Float.parseFloat(splitData[0]);
			index[row][idx] = 0;
			value[row][idx] = classValue;

			for (int col = 1; col < splitData.length; col++) {
				idx++;

				String[] split2 = _pc.split(splitData[col]);
				int colIdx = Integer.parseInt(split2[0]);
				float val = Float.parseFloat(split2[1]);

				index[row][idx] = colIdx;
				value[row][idx] = val;
			}

			row++;
		}
		readData.close();
		return new SparseRowMatrix(numRows, numColumns, index, value);

	}

	public static void createFormatFile(String fn, int rowOffset) throws IOException {
		createFormatFile(new File(fn), rowOffset);
	}

	public static void createFormatFile(File fn, int rowOffset) throws IOException {
		// 1. pass: count #rows, #columns, #triples:
		System.out.println("create format file for " + fn + " (1st pass)..." + " rowOffset = " + rowOffset);

		LineNumberReader read = new LineNumberReader(new FileReader(fn));
		int numTriples = 0;
		int numRows = 0;
		int numCols = 0;
		String line=null;
		while ((line = read.readLine())!=null) {
			if (numTriples % 1000000 == 0)
				System.out.print(numTriples + "\r");
			String[] lne = line.split("\t");
			int rowIdx = Integer.parseInt(lne[0])+ rowOffset;
			int colIdx = Integer.parseInt(lne[1]);
			float val = Float.parseFloat(lne[2]);

			++numTriples;
			if (rowIdx > numRows)
				numRows = rowIdx;
			if (colIdx > numCols)
				numCols = colIdx;
		}
		read.close();

		++numRows;
		++numCols;
		System.out.println();
		System.out.println("detected sparse matrix " + numRows + " x " + numCols + " : " + numTriples + " triple");

		// 2. pass: count #cells in each row:
		System.out.println("create format file for " + fn + " (2nd pass)...");
		int[] rowLength = new int[numRows];
		read = new LineNumberReader(new FileReader(fn));
//		long lineNumber = 0;
		for (int i = 0; i < numTriples; ++i) {
			String[] lne=read.readLine().split("\t");

			int rowIdx = Integer.parseInt(lne[0])+ rowOffset;
//			int colIdx = Integer.parseInt(lne[1]);
//			float val = Float.parseFloat(lne[2]);

			if (rowIdx < 0)
				System.out.println("line " + line);
//			++lineNumber;
			++rowLength[rowIdx];
		}
		read.close();

		// 3. write the format file:
		BufferedWriter out = new BufferedWriter(new FileWriter(fn + ".format"));
		out.write(numRows + "\t" + numCols + "\t" + numTriples + "\t" + rowOffset);
		out.newLine();
		out.write("" + rowLength[0]);
		for (int i = 1; i < numRows; ++i)
			out.write("\t" + rowLength[i]);
		out.newLine();
		out.close();
	}


	public static SparseRowMatrix read(String fn) throws IOException { return read(fn, -1); }

	public static SparseRowMatrix read(String fn, int showProgress) throws IOException {
		return read(new File(fn), showProgress);
	}
	/**
	 * Read a row-sparse matrix from a triples representation: rowIdx, colIdx, value.
	 * Requires a format file with name <fn>.format that contains #rows, #cols, #triples \n #cells in row1, #cells in row2, ...
	 */
	public static SparseRowMatrix read(File fn, int showProgress) throws IOException {
		// 1. read the format file:
		File formatFile = new File(fn.getAbsoluteFile() + ".format");
		if (!formatFile.exists()) {
			System.out.println("Format file does not exist ... creating...");
			createFormatFile(fn, 0);
		}
		LineNumberReader readFormat = new LineNumberReader(new FileReader(formatFile));

		String formatHeader = readFormat.readLine();
		if(null == formatHeader) {
			readFormat.close();
			throw new IOException("Could not read header format");
		}
		String[] split = formatHeader.split("\t");

		int numRows = Integer.parseInt(split[0]);
		int numColumns = Integer.parseInt(split[1]);
		int numTriples = Integer.parseInt(split[2]);
		int rowOffset = Integer.parseInt(split[3]);

		String counts = readFormat.readLine();
		if(null == counts) {
			readFormat.close();
			throw new IOException("Could not read header counts");
		}
		String[] countsSplit = counts.split("\t");

		int[] rowLength = new int[numRows];
		int[][] index = new int[numRows][];
		float[][] value = new float[numRows][];
		for (int i = 0; i < numRows; ++i) {
			rowLength[i] = Integer.parseInt(countsSplit[i]);
			index[i] = new int[rowLength[i]];
			value[i] = new float[rowLength[i]];
		}
		readFormat.close();

		System.out.println("read sparse matrix " + numRows + " x " + numColumns + " : " + numTriples + " triple...");

		// 2. read the triples:
		LineNumberReader readData = new LineNumberReader(new FileReader(fn.getAbsoluteFile()));

		int idx = 0;
		int lastRowIdx = 0;
		for (int i = 0; i < numTriples; ++i) {
			if (i > 0 && i % showProgress == 0) {
				System.out.print("\r " + i);
			}
			String dataLine = readData.readLine();
			if(null == dataLine) {
				readFormat.close();
				readData.close();
				throw new IOException("Could not read row " + i);
			}
			String[] splitData = dataLine.split("\t");

			int rowIdx = Integer.parseInt(splitData[0]) + rowOffset;
			int colIdx = Integer.parseInt(splitData[1]);
			float val = Float.parseFloat(splitData[2]);

			if (rowIdx != lastRowIdx) {
				lastRowIdx = rowIdx;
				idx = 0;
			}
			index[rowIdx][idx] = colIdx;
			value[rowIdx][idx] = val;
			++idx;
		}
		readData.close();
		return new SparseRowMatrix(numRows, numColumns, index, value);
	}

	/**
	 * Read a row-sparse matrix from a triples representation: rowIdx, colIdx, value.
	 * Requires a format file with name <fn>.format that contains #rows, #cols, #triples \n #cells in row1, #cells in row2, ...
	 */
	public static SparseRowMatrix readUnsortedSource(File fn, int showProgress) throws IOException {
		// 1. read the format file:
		File formatFile = new File(fn.getAbsoluteFile() + ".format");
		if (!formatFile.exists()) {
			System.out.println("Format file does not exist ... creating...");
			createFormatFile(fn, 0);
		}
		LineNumberReader readFormat = new LineNumberReader(new FileReader(formatFile));

		String formatHeader = readFormat.readLine();
		if(null == formatHeader) {
			readFormat.close();
			throw new IOException("Could not read header format");
		}
		String[] split = formatHeader.split("\t");

		int numRows = Integer.parseInt(split[0]);
		int numColumns = Integer.parseInt(split[1]);
		int numTriples = Integer.parseInt(split[2]);
		int rowOffset = Integer.parseInt(split[3]);

		String counts = readFormat.readLine();
		if(null == counts) {
			readFormat.close();
			throw new IOException("Could not read header counts");
		}
		String[] countsSplit = counts.split("\t");

		int[] rowLength = new int[numRows];
		int[] rowIdxPos = new int[numRows];
		int[][] index = new int[numRows][];
		float[][] value = new float[numRows][];
		for (int i = 0; i < numRows; ++i) {
			rowLength[i] = Integer.parseInt(countsSplit[i]);
			index[i] = new int[rowLength[i]];
			value[i] = new float[rowLength[i]];
		}
		readFormat.close();

		System.out.println("read sparse matrix " + numRows + " x " + numColumns + " : " + numTriples + " triple...");

		// 2. read the triples:
		LineNumberReader readData = new LineNumberReader(new FileReader(fn.getAbsoluteFile()));

		for (int i = 0; i < numTriples; ++i) {
			if (i > 0 && i % showProgress == 0) {
				System.out.print("\r " + i);
			}
			String dataLine = readData.readLine();
			if(null == dataLine) {
				readFormat.close();
				readData.close();
				throw new IOException("Could not read row " + i);
			}
			String[] splitData = dataLine.split("\t");

			int rowIdx = Integer.parseInt(splitData[0]) + rowOffset;
			int colIdx = Integer.parseInt(splitData[1]);
			float val = Float.parseFloat(splitData[2]);


			index[rowIdx][rowIdxPos[rowIdx]] = colIdx;
			value[rowIdx][rowIdxPos[rowIdx]] = val;
			rowIdxPos[rowIdx]++;
		}
		readData.close();
		return new SparseRowMatrix(numRows, numColumns, index, value);
	}


	public void write(Writer out, int rowOffset) throws IOException {
		for (int i = 0; i < numRows; ++i) {
			int[] idx = index[i];
			float[] val = value[i];
			for (int j = 0; j < idx.length; ++j)
				out.write("" + (i+rowOffset)  + "\t" + idx[j] + "\t" + val[j] + "\n");
		}
	}


	public void write(Writer out, int rowOffset, IntVector columnIndex) throws IOException {
		for (int i = 0; i < numRows; ++i) {
			int[] idx = index[i];
			float[] val = value[i];
			for (int j = 0; j < idx.length; ++j) {
				int col = idx[j];
				int colNew = columnIndex.get(col);
				if (colNew != -1)
					out.write("" + (i+rowOffset)  + "\t" + colNew + "\t" + val[j] + "\n");
			}
		}
	}

	public void debug() {
		int i = 0;
		int[] idx = index[i];
		float[] val = value[i];
		for (int j = 0; j < idx.length; ++j)
			System.out.println("" + i + "\t" + idx[j] + "\t" + val[j]);
	}

	final static int COLSTAT_POPULATED_CELLS = 0;
	final static int COLSTAT_MIN = 1;
	final static int COLSTAT_MAX = 2;
	final static int COLSTAT_SUM = 3;
	final static int COLSTAT_SUM_SQUARES = 4;
	final static int COLSTAT_MEAN = 5;
	final static int COLSTAT_VAR = 6;

	/**
	 * Compute some column statistics:
	 * * each row corresponds to a column in the data set.
	 * * the columns contain the following information:
	 *   0 number of populated cells,
	 *   1 minimum,
	 *   2 maximum,
	 *   3 sum of the cell values,
	 *   4 sum of squared cell values,
	 *   5 mean of cell values,
	 *   6 variance of cell values.
	 */
	public DefaultMatrix columnStatistics() {
		DefaultMatrix stats = new DefaultMatrix(numColumns, 7);
		for (int i = 0; i < numColumns; ++i) {
			stats.set(i, 1, Float.MAX_VALUE);
			stats.set(i, 2, Float.MIN_VALUE);
		}

		// 1. collect the statistics:
		for (int i = 0; i < numRows; ++i) {
			int[] idxi = index[i];
			float[] vali = value[i];
			int numCells = idxi.length;
			for (int jj = 0; jj < numCells; ++jj) {
				int j = idxi[jj];
				float val = vali[jj];
				stats.add(j, COLSTAT_POPULATED_CELLS, 1);
				stats.add(j, COLSTAT_SUM, val);
				stats.add(j, COLSTAT_SUM_SQUARES, val*val);
				if (val < stats.get(j, COLSTAT_MIN))
					stats.set(j, COLSTAT_MIN, val);
				if (val > stats.get(j, COLSTAT_MAX))
					stats.set(j, COLSTAT_MAX, val);
			}
		}

		// 2. compute aggregated values:
		for (int i = 0; i < numColumns; ++i) {
			stats.set(i, COLSTAT_MEAN, stats.get(i, COLSTAT_SUM) / numRows);
			stats.set(i, COLSTAT_VAR, (float) (stats.get(i, COLSTAT_SUM_SQUARES) / numRows - Math.pow(stats.get(i, COLSTAT_MEAN),2)));
		}

		return stats;
	}


	public void encodeCategoricals(String fn, IntVector isNominal, IntVector idxCol) throws IOException {
		// 1. tweak indexCol to support encoding of categorical values
		DefaultIntVector indexCol = new DefaultIntVector(idxCol);
		DefaultMatrix stats = columnStatistics();
		int colInserted = 0;
		for (int i = 0; i < numColumns; ++i) {
			int colNew = indexCol.get(i);
			if (colNew != -1) {
				indexCol.set(i, colNew + colInserted);
				if (isNominal.get(i) == 1) {
					System.out.println("ins " + i + " --> " + colNew + " --> " + (colNew + colInserted) + ": " + ((stats.get(i, COLSTAT_MAX) - stats.get(i, COLSTAT_MIN) + 1)));
					colInserted += (stats.get(i, COLSTAT_MAX) - stats.get(i, COLSTAT_MIN) + 1);
				}
			}
		}
		System.out.println("inserting " + colInserted + " additional categorical indicator columns");

		// 2. now write the triples:
		BufferedWriter out = new BufferedWriter(new FileWriter(fn));
		for (int i = 0; i < numRows; ++i) {
			int[] idx = index[i];
			float[] val = value[i];
			for (int j = 0; j < idx.length; ++j) {
				int col = idx[j];
				int colNew = indexCol.get(col);
				if (colNew != -1) {
					if (isNominal.get(col) == 1)
						out.write("" + i + "\t" + (int)(colNew + val[j] - stats.get(col, COLSTAT_MIN)) + "\t1\n");
					else
						out.write("" + i + "\t" + colNew + "\t" + val[j] + "\n");
				}
			}
		}
		out.close();
	}


	public void writeLibSVM(String fn, IntVector target, IntVector split, int splitValue) throws IOException {
		System.out.println("write " + fn + "...");
		BufferedWriter out = new BufferedWriter(new FileWriter(fn));
		for (int i = 0; i < numRows; ++i) {
			if (split.get(i) == splitValue) {
				int[] idx = index[i];
				float[] val = value[i];
				if (splitValue != 2)
					out.write("" + target.get(i)); // train
				else
					out.write("0"); // test
				for (int j = 0; j < idx.length; ++j)
					out.write(" " + (idx[j]+1) + ":" + val[j]);
				out.write("\n");
			}
		}
		out.close();
	}

	/**
	 * Divide all cells by the absolute maximum value of its column.
	 */
	public void normalizeRange() {
		// 1. compute absolute maximum per column:
		Matrix stats = columnStatistics();
		float[] absmax = new float[numColumns];
		for (int i = 0; i < numColumns; ++i)
			absmax[i] = Math.max(-stats.get(i, COLSTAT_MIN), stats.get(i, COLSTAT_MAX));

		// 2. rescale:
		for (int i = 0; i < numRows; ++i) {
			int[] idx = index[i];
			float[] val = value[i];
			for (int j = 0; j < idx.length; ++j)
				val[j] /= absmax[ idx[j] ];
		}
	}

	public String toString() {
		return Matrices.toString(this);
	}

}