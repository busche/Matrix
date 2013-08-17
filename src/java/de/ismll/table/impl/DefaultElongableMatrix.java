package de.ismll.table.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;

import de.ismll.table.Matrix;

public class DefaultElongableMatrix implements Matrix{

	private final int columns;

	public DefaultElongableMatrix(int columns) {
		this.columns = columns;
		// stores (2^19)-1 values
		values = new float[20][][];
		values[currentBinIdx] = new float[1][columns];
	}
	/*
	 * [0][2][columns]
	 * [1][4][columns]
	 * [2][8][columns]
	 * [3][16][columns]
	 * [4][32][columns]
	 * [5][64][columns]
	 */
	float[][][] values;

	public static DefaultElongableMatrix read(InputStream is, char tokenizer) throws IOException {
		LineNumberReader read = new LineNumberReader(new InputStreamReader(is));
		String line = null;
		DefaultElongableMatrix ret = null;
		int numcolumns = -1;
		float[] buffer = null;
		while ((line = read.readLine())!=null) {
			String[] split = line.split(Character.toString(tokenizer));
			if (read.getLineNumber()==1) {
				// first line
				numcolumns = split.length;
				ret = new DefaultElongableMatrix(numcolumns);
				buffer = new float[numcolumns];
			}
			if ("".equals(line)) {
				// last line.
				break;
			}
			for (int i = 0; i < numcolumns; i++) {
				String string = split[i];
				if ("".equals(string)) {
					buffer[i] = Float.NaN;
					continue;
				}
				Float valueOf = null;
				try{
					valueOf = Float.valueOf(string);
				} catch (Exception e) {
					continue;
				}

				buffer[i] = valueOf.floatValue();
			}
			ret.append(buffer);
		}
		return ret;
	}

	protected int base = 2;
	protected int currentBinIdx = 0;
	protected int currentInBinIdx = 0;

	protected void append(float[] buffer) {
		if (currentInBinIdx == values[currentBinIdx].length) {
			currentInBinIdx=0;
			currentBinIdx++;
		}

		if (values[currentBinIdx] == null) {
			values[currentBinIdx] =new float[(int) Math.pow(base, currentBinIdx)][columns];
		}

		System.arraycopy(buffer, 0, values[currentBinIdx][currentInBinIdx++], 0, columns);
	}

	public float get(int rowIndex, int columnIndex) {
		int[] bin = getBin(rowIndex);
		return values[bin[0]][bin[1]][columnIndex];
	}

	private int[] getBin(int rowIndex) {
		int[] ret = new int[2];
		int tm = rowIndex;
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null) {
				throw new RuntimeException("Matrix is too long. Index is not supported!");
			}
			if (tm - values[i].length >=0) {
				tm -= values[i].length;
				ret[0]++;
			} else {
				ret[1] = tm;
				// found
				i = values.length;
			}
		}
		return ret;
	}

	public int getNumColumns() {
		return columns;
	}

	public int getNumRows() {
		if (currentBinIdx == 0) {
			return currentInBinIdx;
		}
		int ret = 0;
		for (int i = 0; i < currentBinIdx-1; i++) {
			ret += values[i].length * base;
		}
		return ret + currentInBinIdx+1;
		//		return (int) (Math.pow(base, currentBinIdx-1) + currentInBinIdx + 1);
	}

	public void set(int rowIndex, int columnIndex, float value) {
		int[] bin = getBin(rowIndex);

		values[bin[0]][bin[1]][columnIndex] = value;
	}

	public void dump() {
		System.out.println("Total rows: " + getNumRows());
		for (int i = 0; i < values.length; i++) {
			System.out.println("Bin " + i + ": length " + (values[i] == null?"null":values[i].length + ""));
			System.out.println("\t" + Arrays.deepToString(values[i]));

		}

	}

}
