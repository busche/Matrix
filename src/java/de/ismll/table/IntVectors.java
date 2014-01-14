/**
 * IntVectors.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
// import java.util.*;

import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.impl.DefaultVector;


/**
 * Some utiltiy functions for IntVectors.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public class IntVectors {

	/**
	 * Compute the maximum.
	 */
	public static int max(IntVector x) {
		int max = x.get(0);
		int size = x.size();
		for (int i = 1; i < size; ++i)
			if (x.get(i) > max)
				max = x.get(i);
		return max;
	}

	public static int min(IntVector x) {
		int min = x.get(0);
		int size = x.size();
		for (int i = 1; i < size; ++i)
			if (x.get(i) < min)
				min = x.get(i);
		return min;
	}

	/**
	 * Compute the sum.
	 */
	public static long sum(IntVector x) {
		long sum = 0;
		int size = x.size();
		for (int i = 0; i < size; ++i)
			sum += x.get(i);
		return sum;
	}

	/**
	 * Set all values of a vector.
	 */
	public static void set (IntVector x, int value) {
		int size = x.size();
		for (int i = 0; i < size; ++i)
			x.set(i, value);
	}


	/**
	 * Count how many entries with a given value there are.
	 */
	public static int count(IntVector x, int value) {
		int num = 0;
		int size = x.size();
		for (int i = 0; i < size; ++i)
			if (x.get(i) == value)
				++num;
		return num;
	}

	public static void write(IntVector x, String fn) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(fn));
		int size = x.size();
		out.write("" + size);
		out.newLine();
		for (int i = 0; i < size; ++i) {
			out.write("" + x.get(i));
			out.newLine();
		}
		out.close();
	}


	public static void write(IntVector x,
			File fn) throws IOException {
		write(x, fn, true);
	}
	public static void write(IntVector x,
			File fn, boolean header) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(fn));
		int size = x.size();
		if (header) {
			out.write("" + size);
			out.newLine();
		}
		for (int i = 0; i < size; ++i) {
			out.write("" + x.get(i));
			out.newLine();
		}
		out.close();
	}

	public static void print(IntVector x) {
		int size = x.size();
		if (size == 0) {
			System.out.println("vector of size 0");
			return;
		}
		System.out.print(x.get(0));
		for (int i = 1; i < size; ++i)
			System.out.print(" " + x.get(i));
		System.out.println();
	}

	/**
	 * Put a random permutation of the numbers 0..size-1 into x.
	 */
	public static void randomPermutation(IntVector x) {
		int n = x.size();
		int[] numbers = new int[n];
		for (int i = 0; i < n; ++i)
			numbers[i] = i;
		int nrest = n;
		for (int i = 0; i < n; ++i) {
			// select a remaining number for the next place:
			int idx = (int) Math.floor(Math.random() * nrest);
			x.set(i, numbers[idx]);
			// now delete this number:
			for (int j = idx; j < nrest-1; ++j)
				numbers[j] = numbers[j+1];
			--nrest;
		}
	}

	/**
	 * Put a random integer between from..to into each cell of the vector.
	 */
	public static void randomInteger(IntVector x, int from, int to) {
		int n = x.size();
		for (int i = 0; i < n; ++i)
			x.set(i, (int) Math.floor(Math.random() * (to-from) + from));
	}

	/**
	 * Put a random integer between from..to into each cell of the vector.
	 * Put <from> with probability <prob[0]>, <from+1> with probability <prob[1]>,...
	 */
	public static void randomInteger(IntVector x, int from, Vector prob) {
		// 1. compute accumulated probabilities:
		DefaultVector accum = new DefaultVector(prob.size());
		accum.set(0, prob.get(0));
		for (int i = 1; i < prob.size(); ++i)
			accum.set(i, prob.get(i) + accum.get(i-1));
		Vectors.print(accum);

		// 2. draw the numbers:
		int n = x.size();
		for (int i = 0; i < n; ++i) {
			float p = (float) Math.random();
			int idx = 0;
			while (idx < prob.size() && p > accum.get(idx))
				++idx;
			x.set(i, idx + from);
		}
	}

	public static void fill(DefaultIntVector v, int value) {
		int size = v.size();
		for (int i = 0; i < size; i++)
			v.set(i, value);

	}

}