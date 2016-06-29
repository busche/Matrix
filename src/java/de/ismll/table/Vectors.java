/**
 * Vectors.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.table;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;

import de.ismll.processing.Normalizer;
import de.ismll.table.impl.DefaultBitVector;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.ColumnSubsetVectorView;
import de.ismll.table.projections.RowSubsetVector;
import de.ismll.table.projections.RowSubsetVectorView;
import de.ismll.table.projections.VectorSubset;
import de.ismll.utilities.Assert;
import de.ismll.utilities.Buffer;

/**
 * Some utiltiy functions for Vectors.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
/**
 * @author Andre Busche
 *
 */
public class Vectors {

	/**
	 * Compute the maximum.
	 */
	public static float max(Vector x) {
		float max = x.get(0);
		int x_size = x.size();
		for (int i = 1; i < x_size; ++i) {
			float x_i = x.get(i);
			if (x_i > max)
				max = x_i;
		}
		return max;
	}


	/**
	 * Compute the index of the maximum entry of a Vector.
	 */

	/**
	 * @return the index of the maximal values
	 */
	public static int maxIdx(Vector x) {
		float max = x.get(0);
		int idx= 0;
		int x_size = x.size();
		for (int i = 1; i < x_size; ++i) {
			float x_i = x.get(i);
			if (x_i > max) {
				max = x_i;
				idx=i;
			}
		}
		return idx;
	}


	/**
	 * Compute the maximum.
	 */
	public static float min(Vector x) {
		int x_size = x.size();
		if (x_size == 0)
			return Float.NaN;
		float min = x.get(0);
		for (int i = 1; i < x_size; ++i) {
			float x_i = x.get(i);
			if (x_i < min)
				min = x_i;
		}
		return min;
	}

	public static int minIdx(Vector x) {
		int x_size = x.size();
		if (x_size == 0)
			return -1;
		float min = x.get(0);
		int idx = 0;
		for (int i = 1; i < x_size; ++i) {
			float x_i = x.get(i);
			if (x_i < min) {
				min = x_i;
				idx = i;
			}
		}
		return idx;
	}

	/**
	 * Compute the sum.
	 */
	public static float sum(Vector x) {
		float sum = 0;
		int x_size = x.size();
		for (int i = 0; i < x_size; ++i)
			sum += x.get(i);
		return sum;
	}

	/**
	 * Compute the sum.
	 */
	public static float sum(IntVector x) {
		int sum = 0;
		int size = x.size();
		for (int i = 0; i < size; ++i)
			sum += x.get(i);
		return sum;
	}


	/**
	 * Set all values of a vector.
	 */
	public static void set (Vector x, float value) {
		int x_size = x.size();
		for (int i = 0; i < x_size; ++i)
			x.set(i, value);
	}

	/**
	 * Fill the vector uniformly at random.
	 */
	public static void fillUniformAtRandom(Vector x, float from, float to) {
		for (int i = 0; i < x.size(); ++i)
			x.set(i, (float) Math.random() * (to-from) + from);
	}

	/**
	 * Use WriterConfig instead
	 */
	@Deprecated
	public static void write(Vector x, File file, boolean writeHeader) throws IOException {
		PrintStream ps = null;
		if (!file.exists())
			file.createNewFile();
		try {
			ps = new PrintStream(Buffer.newOutputStream(file));
			if (writeHeader)
				ps.println("" + x.size());

			for (int i = 0; i < x.size(); ++i)
				ps.println("" + x.get(i));

		} catch (IOException e) {
			throw e;
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public static void write(Vector x, File fn) throws IOException {
		write(x, fn, true);
	}

	/**
	 * Don't use Strings
	 */
	@Deprecated
	public static void write(Vector x, String fn) throws IOException {
		write(x, new File(fn));
	}


	public static void write(IntVector x, File file) throws IOException {
		PrintStream ps = null;
		try {
			ps = new PrintStream(file);
			int size = x.size();
			ps.println("" + size);

			for (int i = 0; i < size; ++i)
				ps.println("" + x.get(i));

		} catch (IOException e) {
			throw e;
		} finally {
			if (ps != null)
				ps.close();
		}
	}
	/**
	 * use toString() instead
	 */
	@Deprecated
	public static void print(Vector x) {
		if (x.size() == 0) {
			System.out.println("vector of size 0");
			return;
		}
		System.out.print(x.get(0));
		for (int i = 1; i < x.size(); ++i)
			System.out.print(" " + x.get(i));
		System.out.println();
	}


	private static final class VectorMCallback implements MatrixCallback {
		Vector r;

		@Override
		public void setField(int row, int col, String string) {
			r.set(row, Float.parseFloat(string));
		}

		@Override
		public void meta(int numRows, int numColumns) {
			Assert.assertTrue(numColumns==1, "VectorMCallback may only get one column");
			r = new DefaultVector(numRows);
		}
	}


	public static class Entry implements Comparable<Entry> {
		public int index;
		public float value;
		public Entry(int index, float value) { this.index = index; this.value = value; }
		public int compareTo(Entry other) {
			if (value < other.value)
				return +1;
			else if (value < other.value)
				return -1;
			else
				return 0;
		}
	}
	// urghh
	public static IntVector order(Vector x) {
		// create entry objects (urgh)
		int x_size = x.size();
		Entry[] entries = new Entry[x_size];
		for (int i = 0; i < x_size; ++i)
			entries[i] = new Entry(i, x.get(i));
		Arrays.sort(entries);

		// copy order:
		IntVector order = new DefaultIntVector(x_size);
		for (int i = 0; i < x_size; ++i)
			order.set(i, entries[i].index);
		return order;
	}

	public static void sort(IntVector cp) {
		if (cp instanceof DefaultIntVector) {
			Arrays.sort(((DefaultIntVector)cp).data);
		} else {
			throw new RuntimeException("By now, only can sort DefaultIntVectors.");
		}
	}



	public static void sort(Vector cp) {
		if (cp instanceof DefaultVector) {
			Arrays.sort(((DefaultVector)cp).data);
		} else {
			throw new RuntimeException("By now, only can sort DefaultIntVectors.");
		}
	}

	/**
	 * Will return NaN if one of the values in the vector is Nan
	 */
	public static double average(Vector v) {
		double sum = 0.;
		int v_size = v.size();
		for (int i = 0; i < v_size; i++)
			sum += v.get(i);

		return sum/v_size;
	}

	/**
	 * 
	 * Will return NaN if one of the values in the vector is Nan
	 *
	 */
	public static double variance(Vector v) {
		double a = 0, c=0;
		double mean2 = average(v);
		int v_size = v.size();
		for (int i=0;i<v_size;i++) {
			float v_i = v.get(i);
			a+=((v_i-mean2)*(v_i-mean2));
			c++;
		}
		return Math.sqrt( a/c );
	}

	/**
	 * Counts the number of occurrences of value with the given epsilon around that value
	 * 
	 */
	public static int count(Vector vector, float value, float epsilon) {
		int cnt = 0;
		for (int i = 0; i < vector.size(); i++) {
			float vectorValue = vector.get(i);
			if (vectorValue-epsilon<value
					&& vectorValue+epsilon>value)
				cnt++;
		}
		return cnt;
	}

	public static int count(IntVector vector, int value) {
		int cnt = 0;
		for (int i = 0; i < vector.size(); i++) {
			int vectorValue = vector.get(i);
			if (vectorValue==value)
				cnt++;
		}
		return cnt;
	}

	@Deprecated
	public static void readDenseSlow(File f, VectorCallback cb) throws FileNotFoundException {
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			throw e;
		}
		Scanner sc = new Scanner(in);
		int size = sc.nextInt();
		cb.meta(size);
		for (int i = 0; i < size; ++i)
			cb.setField(i, sc.nextFloat() + "");
		sc.close();
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * TODO: Move this to some kind of "convert" method, to dis-confuse "read" and "parse"
	 */
	public static Vector convert(String str) throws IOException {

		ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
		Vector ret = readDense(bais);
		bais.close();
		return ret;
	}

	/**
	 * converts [0,1,1,0,0] to [1,2]
	 * 
	 * Move to BitVectors, or DefaultBitVector
	 */
	@Deprecated
	public static IntVector convert2Pointers(BitVector pointers) {

		DefaultIntVector ret;
		int size = 0;
		for (int i = 0; i < pointers.size(); i++)
			if (pointers.get(i))
				size++;
		ret = new DefaultIntVector(size);
		int pos = 0;
		for(int i = 0; i < pointers.size(); ++i)
			if (pointers.get(i)) {
				ret.set(pos, i);
				++pos;
			}
		return ret;
	}

	/**
	 * converts [0,1,2,0,1,1];1 to [1,4,5]
	 * 
	 * and [0,1,2,0,1,1];2 to [2]
	 * 
	 */
	public static IntVector convert2Pointers(IntVector split, int value2Seek_n_extract) {
		int size = IntVectors.count(split, value2Seek_n_extract);
		DefaultIntVector index = new DefaultIntVector(size);
		int pos = 0;
		for(int i = 0; i < split.size(); ++i)
			if (split.get(i) == value2Seek_n_extract) {
				index.set(pos, i);
				++pos;
			}
		return index;
	}

	/**
	 * use {@link #convert2Pointers(Vector, int, float)} instead.
	 */
	@Deprecated
	public static IntVector convert2Pointers(Vector split, int splitValue) {
		int size = Vectors.count(split, splitValue, 0.01f);
		DefaultIntVector index = new DefaultIntVector(size);
		int pos = 0;
		for(int i = 0; i < split.size(); ++i)
			if (Math.abs(( split.get(i) - splitValue))<0.01f) {
				index.set(pos, i);
				++pos;
			}
		return index;
	}

	public static IntVector convert2Pointers(Vector split, int splitValue, float threshold) {
		int size = Vectors.count(split, splitValue, threshold);
		DefaultIntVector index = new DefaultIntVector(size);
		int pos = 0;
		for(int i = 0; i < split.size(); ++i)
			if (Math.abs(( split.get(i) - splitValue))<threshold) {
				index.set(pos, i);
				++pos;
			}
		return index;
	}


	public static Vector readDense(File fn) throws IOException  {
		return readDense(Buffer.newInputStream(fn));
	}

	public static Vector readDense(InputStream is) throws IOException  {
		ReaderConfig rc = new ReaderConfig();

		return readDense(is, rc);
	}

	public static Vector readDense(File fn, ReaderConfig rc) throws IOException  {
		InputStream newInputStream = Buffer.newInputStream(fn);
		try{
			return readDense(newInputStream, rc);
		} finally {
			newInputStream.close();
		}
	}

	public static Vector readDense(InputStream is, ReaderConfig rc) throws IOException {
		VectorMCallback vectorReader = new VectorMCallback();
		rc.numColumns=1;

		Matrices.readDense(is, rc, vectorReader);

		return vectorReader.r;
	}


	/**
	 * Use ReaderConfig instead
	 */
	@Deprecated
	public static void readDense(File fn, int showProgress, VectorCallback c) throws FileNotFoundException {
		BufferedInputStream fis;
		try {
			fis = new BufferedInputStream(new FileInputStream(fn));
		} catch (FileNotFoundException e1) {
			throw e1;
		}

		byte[] buffer = new byte[Buffer.getBufferSize(fn)];
		StringBuilder sb = new StringBuilder();

		int numRows = -1;

		int read;
		boolean headerRead= false;
		int rowNr = 0;

		boolean previousLinebreak=false;

		try {
			while((read = fis.read(buffer))>0 ) {

				for (int i = 0; i < read; i++) {
					byte b = buffer[i];
					boolean lineBreak = b == '\n' || b == '\r';

					if (!headerRead) {
						// seek for numCols and numRows
						sb.append((char)b);

						if (lineBreak) {
							headerRead=true;

							numRows = Integer.parseInt(sb.toString().trim());
							c.meta(numRows);
							sb.setLength(0);
						}
					} else {
						// header read; parse lines...

						if (lineBreak && !previousLinebreak) {
							// parse value
							c.setField(rowNr, sb.toString());
							sb.setLength(0);
						}
						else {
							// trim whitespace
							if (b != ' ' && !lineBreak)
								sb.append((char)b);
						}

						if (lineBreak && !previousLinebreak) {
							rowNr++;
							if (rowNr>0 && rowNr % showProgress == 0)
								System.out.println(rowNr);

						}

					}
					previousLinebreak=lineBreak;
				}
			}
			if ( sb.length()>0) {
				c.setField(rowNr, sb.toString());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}


	public static String toString(Vector vector) {
		StringBuilder sb = new StringBuilder();
		String s = "\n";
		int vec_size = vector.size();
		sb.append(vec_size + s);
		for (int i = 0; i < vec_size; i++)
			sb.append(vector.get(i) + s);

		return sb.toString();
	}
	public static String toString(IntVector vector) {
		StringBuilder sb = new StringBuilder();
		String s = "\n";
		int vec_size = vector.size();
		sb.append(vec_size + s);
		for (int i = 0; i < vec_size; i++)
			sb.append(vector.get(i) + s);

		return sb.toString();
	}

	public static double covariance(Vector vec1, Vector vec2) {
		int vec1_size = vec1.size();
		if (vec1_size!=vec2.size()) return Double.NaN;
		double averageVec1 = average(vec1);
		double averageVec2 = average(vec2);
		double sum = 0.;
		for (int i = 0; i < vec1_size; i++)
			sum += (vec1.get(i)-averageVec1)*
			(vec2.get(i)-averageVec2);

		return sum / vec1_size;
	}


	public static double correlation(Vector vec1, Vector vec2) {
		int vec1_size = vec1.size();
		if (vec1_size!=vec2.size()) return Double.NaN;
		double averageVec1 = average(vec1);
		double averageVec2 = average(vec2);
		double varianceVec1 = variance(vec1);
		double varianceVec2 = variance(vec2);
		double sum = 0.;
		for (int i = 0; i < vec1_size; i++)
			sum += (vec1.get(i)-averageVec1)*(vec2.get(i)-averageVec2);

		return (sum / vec1_size)/(varianceVec1*varianceVec2);
	}


	public static Iterable<Integer> iterable(final IntVector inputVector) {
		return new Iterable<Integer>() {

			@Override
			public Iterator<Integer> iterator() {
				return Vectors.iterator(inputVector);
			}
		};

	}


	protected static Iterator<Integer> iterator(IntVector inputVector) {
		return new IntVectorIterator(inputVector);
	}


	/**
	 * FIXME: DOCUMENT!!
	 */
	public static IntVector removeAll(IntVector baseSet, IntVector removeEntries) {
		// assumption: both are ordered.
		int lastA=-1, lastB=-1;
		int idxA=0, idxB=0;
		int valB = removeEntries.get(idxB);
		int numCommons = 0;
		int baseset_size = baseSet.size();
		int removeentries_size = removeEntries.size();
		while (idxA < baseset_size) {
			int nextA = baseSet.get(idxA++);
			assert (nextA> lastA);
			lastA = nextA;
			while (valB < nextA && idxB < removeentries_size) {
				valB = removeEntries.get(idxB++);
				assert (valB> lastB);
				lastB = valB;
			}
			if (valB == nextA)
				numCommons++;
		}
		int retSize = baseset_size-numCommons;
		DefaultIntVector ret = new DefaultIntVector(retSize);
		int retIdx=0;
		idxA=0;
		idxB=0;
		valB = removeEntries.get(idxB);
		while (idxA < baseset_size) {
			int nextA = baseSet.get(idxA++);
			while (valB < nextA && idxB < removeentries_size) {
				valB = removeEntries.get(idxB++);
			}
			if (valB != nextA)
				ret.set(retIdx++, nextA);
		}
		return ret;
	}


	/**
	 * note: does NOT normalize, you need to call {@link Normalizer#normalize(Vector)} also!
	 
	 */
	public static Normalizer normalize(Vector v) {
		double average = average(v);
		double variance = variance(v);
		Normalizer ret = new Normalizer(average,variance);

		return ret;
	}


	public static BitVector asBitvector(IntVector in, int splitValue) {
		int size = in.size();
		BitVector ret = new DefaultBitVector(size);
		int idx = 0;
		for (int i = 0; i < size; i++)
			ret.set(idx++, in.get(i)==splitValue);
		return ret;
	}


	private static boolean warnedRows=false;

	public static Vector rows(Vector source, IntVector selector, boolean writes) {
		if (!warnedRows) {
			System.err.println("write-only not yet implemented! returning always a writable view! This message will appear only once!");
			warnedRows=true;
		}

		VectorSubset vs = new VectorSubset(source, selector);
		return vs;
	}

	public static Vector row(Matrix use, int r) {
		return new RowSubsetVectorView(use, r);
	}

	/**
	 * Name ambigous
	 */
	@Deprecated
	public static void set(Vector target, IntVector source) {
		for (int i = 0; i < source.size(); i++) {
			target.set(i, source.get(i));
		}
	}


	public static void set(IntVector target, int value) {
		int size = target.size();
		for (int i = 0; i < size; i++) {
			target.set(i, value);
		}
	}

	/**
	 * fills the given vector with consecutive integers in range [from,to[. Starts at index 0 in target.
	 * 
	 */
	public static void fill(IntVector target, int from, int to) {
		for (int i = from; i < to; i++)
			target.set(i-from, i);
	}

	public static Vector rows(Vector source, BitVector selector, boolean writes) {
		return new RowSubsetVector(source, selector);
	}

	public static Vector col(Matrix leftValues, int i) {
		return new ColumnSubsetVectorView(leftValues, i);
	}

	public static <E extends Number> Vector asVector(Collection<E> collection) {
		DefaultVector ret = new DefaultVector(collection.size());
		Iterator<E> iterator = collection.iterator();
		int idx=0;
		while(iterator.hasNext())
			ret.data[idx++]=iterator.next().floatValue();

		return ret;
	}

	/**
	 * Move to BitVectors
	 */
	@Deprecated
	public static void set(BitVector mask, boolean b) {
		for (int i = 0; i < mask.size(); i++)
			mask.set(i, b);
	}

	public static float quantile(DefaultVector dv, float f) {
		float[] copy = new float[dv.data.length];
		System.arraycopy(dv.data, 0, copy, 0, dv.data.length);

		Arrays.sort(copy);

		return copy[(int) (f*copy.length)];

	}
	public static int[] toIntArray(Vector v) {
		int v_size = v.size();
		int[] ret = new int[v_size];
		for (int i = 0; i < v_size; i++)
			ret[i]=(int) v.get(i);


		return ret;
	}

	public static Vector floatArraytoVector (float[] in) {
		return new DefaultVector(in);
	}

	public static float[] toFloatArray(Vector v) {
		int v_size = v.size();
		float[] ret = new float[v_size];
		for (int i = 0; i < v_size ; i++) {
			ret[i] = v.get(i);
		}
		return ret;
	}

	public static int[] toIntArray(IntVector v) {
		int v_size = v.size();
		int[] ret = new int[v_size];
		for (int i = 0; i < v_size; i++)
			ret[i]=v.get(i);

		return ret;
	}
	public static double dotProduct(Vector a, Vector b) {
		double sum = 0;
		int a_size = a.size();
		for (int i = 0; i < a_size; i++)
			sum += a.get(i)*b.get(i);
		return sum;
	}

	public static double norm(Vector a) {
		double ret = 0;
		int a_size = a.size();
		for (int i = 0; i < a_size; i++) {
			ret += a.get(i)*a.get(i);
		}

		return Math.sqrt(ret);
	}


	public static void add(Vector in, float value) {
		int in_size = in.size();
		for (int i = 0; i < in_size; i++)
			in.set(i, in.get(i) +value);
	}



	/**
	 * UGLY - but working.
	 * 
	 * Encodes the String array as a Vector and returns it. *Extremely* inefficient, but works.
	 * 
	 */
	public static Vector encodeUglyStringArray(String[] options) {
		if (options == null) return new DefaultVector(0);
		if (options.length == 0) return new DefaultVector(0);

		int length = 0;
		length++; // length of array

		for (String s : options) {
			length ++; // add one for length value
			length += s.length(); // content
		}

		DefaultVector ret = new DefaultVector(length);

		int idx=0;
		ret.data[idx++] = options.length;
		for (String s : options) {
			ret.data[idx++] = s.length();
			for (byte b : s.getBytes()) {
				ret.data[idx++] = b;
			}
		}

		return ret;
	}

	/**
	 * 
	 * parses the Vector to return the same content as used while encoding through {@link #encodeUglyStringArray(String[])}
	 * 
	 */
	public static String[] decodeUglyStringVector(Vector in) {
		String[] ret = new String[(int) in.get(0)];
		int retIdx=0;
		StringBuffer sb = new StringBuffer();

		int stringLength=-1;
		int in_size = in.size();
		for (int i = 1; i < in_size; i++) {
			if(stringLength < 0) {
				stringLength = (int) in.get(i);
				continue;
			}
			if (sb.length() < stringLength) {
				sb.append((char)in.get(i));
			} else {
				ret[retIdx++]=sb.toString();
				sb.setLength(0);
				stringLength=(int) in.get(i);
			}

		}
		if (sb.length()>0) {
			ret[retIdx++]=sb.toString();
		}

		return ret;
	}


	/**
	 * copies all values from from to to.
	 * 
	 */
	public static void copy(Vector from, Vector to) {
		int from_size = from.size();
		if (from_size!=to.size()) throw new RuntimeException("Unequal vector length: (" + from_size + "!=" + to.size() + ")");
		for (int i = 0; i < from_size; i++) {
			to.set(i, from.get(i));
		}

	}


	public static double var(Vector c) {
		float a = 0, c_=0;
		double mean2 = avg(c);
		int c_size = c.size();
		for (int i=0;i<c_size;i++) {
			float c_i = c.get(i);
			a+=((c_i-mean2))*((c_i-mean2));
			c_++;
		}
		return Math.sqrt( a/c_ );
	}


	public static double avg(Vector c) {
		float a = 0, c_ = 0;
		int c_size = c.size();
		for (int i=0;i<c_size;i++) {
			a+=c.get(i);
			c_++;
		}
		return ( a/c_ );
	}


	/**
	 * sets all data from data to sink.
	 * 
	 */
	public static void set(Vector sink, Vector data) {
		int data_size = data.size();
		for (int i = 0; i < data_size; i++)
			sink.set(i, data.get(i));
	}


}

