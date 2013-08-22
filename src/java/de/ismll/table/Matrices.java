/**
 * Matrices.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.table;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import de.ismll.converter.Arff2Ismll;
import de.ismll.table.impl.DefaultAnnotatedMatrix.DefaultAnnotatedMatrixParser;
import de.ismll.table.impl.DefaultIntMatrix;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultMatrix.DefaultMatrixParser;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.io.weka.ArffDataset;
import de.ismll.table.io.weka.ArffEncoder;
import de.ismll.table.io.weka.ArffEncoderHelper;
import de.ismll.table.io.weka.IsmllArffDataset;
import de.ismll.table.projections.ColumnSubsetIntVectorView;
import de.ismll.table.projections.ColumnSubsetMatrixView;
import de.ismll.table.projections.ColumnSubsetVectorView;
import de.ismll.table.projections.RowSubsetMatrixView;
import de.ismll.table.projections.RowSubsetVectorView;
import de.ismll.utilities.Assert;
import de.ismll.utilities.Buffer;
import de.ismll.utilities.Tools;

/**
 * Some utiltiy functions for matrices.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public class Matrices {

	public static final byte BINARY_UNCOMPRESSED = 0x00;
	public static final byte BINARY_COMPRESSED = 0x01;


	public static final class IsmllArffEncoder implements ArffEncoder, IsmllArffEncoderMapper {
		TreeMap<Integer,String> wekaType;
		TreeMap<Integer,String> wekaName;
		IsmllArffEncoder(){
			wekaType=new TreeMap<Integer, String>();
			wekaName=new TreeMap<Integer, String>();
			unmapper=new HashMap<Integer, Map<Integer,String>>();
		}
		@Override
		public String getName() {
			return datasetname;
		}

		/* (non-Javadoc)
		 * @see de.ismll.table.IsmllArffEncoderMapper#getMap(int)
		 */
		@Override
		public Map<Integer, String> getMap(int column) {
			return unmapper.get(Integer.valueOf(column));
		}

		@Override
		public Type getAttributeType(int column) {
			if (wekaType.get(Integer.valueOf(column)).equalsIgnoreCase(NUMERIC))
				return Type.Numeric;
			// TODO: this is an assumption!!
			return Type.Nominal;
		}

		@Override
		public String getAttributeName(int column) {
			return wekaName.get(Integer.valueOf(column));
		}

		@Override
		public String encode(int column, float value) {
			if (getAttributeType(column).equals(Type.Numeric))
				return "" + value;
			Integer i = Integer.valueOf(column);
			if (unmapper.containsKey(i)) {
				Map<Integer, String> m = unmapper.get(i);
				String string = m.get(Integer.valueOf(Float.valueOf(value).intValue()));
				if (string == null)
					return "?";
				return string;
			}
			return "" + value;
		}

		private Map<Integer, Map<Integer, String>> unmapper;
		public String datasetname;

		public void setNominalMap(Integer column, Map<Integer, String> unmap) {
			unmapper.put(column, unmap);
		}
		public void setAName(int currentAttribute, String name) {
			wekaName.put(Integer.valueOf(currentAttribute), name);

		}
		public void setAType(int currentAttribute, String name) {
			wekaType.put(Integer.valueOf(currentAttribute), name);
		}

		@Override
		public int getNumColumns() {
			return wekaType.size();
		}
	}

	public enum FileType {
		Binary, Ismll, Arff, Csv, HeaderlessCsv, AnnotatedIsmll
	}

	public enum Mirror {
		HORIZONTAL, VERTICAL

	}

	public static ReaderConfig headerlessCsvReaderConfig;

	static {
		headerlessCsvReaderConfig = new ReaderConfig();
		headerlessCsvReaderConfig.autodetectFormat=true;

	}

	private static final String META_DELIMITER = ",";
	private static final String VALUES_DELIMITER = ",";
	public static boolean debug;

	private static Logger logger = LogManager.getLogger(Matrices.class);

	static {
		if (!debug) {
			debug = logger.isDebugEnabled();
		}
	}




	// ----------------------------------------------------------------------
	// Fill:

	/**
	 * Fill the matrix uniformly at random.
	 */
	public static void fillUniformAtRandom(Matrix x, float from, float to) {
		int numRows = x.getNumRows();
		int numColumns = x.getNumColumns();
		Random r = new Random();
		for (int i = 0; i < numRows; ++i) {
			for (int j = 0; j < numColumns; ++j)
				x.set(i, j, r.nextFloat() * (to-from) + from);
		}
	}

	// ----------------------------------------------------------------------
	// IO:



	public static void print(Matrix x) {
		int numRows = x.getNumRows();
		int numColumns = x.getNumColumns();
		if (numRows == 0 || numColumns == 0) {
			System.out.println("matrix with 0 rows and/or columns");
			return;
		}
		for (int i = 0; i < numRows; ++i) {
			System.out.print(x.get(i,0));
			for (int j = 1; j < numColumns; ++j)
				System.out.print(" " + x.get(i,j));
			System.out.println();
		}
	}

	public static double normL1(Matrix x) {
		double norm = 0;
		int numRows = x.getNumRows();
		int numColumns = x.getNumColumns();
		for (int i = 0; i < numRows; ++i) {
			for (int j = 0; j < numColumns; ++j)
				norm += Math.abs(x.get(i,j));
		}
		return norm;
	}



	public static void writeSparse(Matrix x, String fn) throws IOException {
		File file = new File(fn);
		writeSparse(x, file);
	}

	public static void writeSparse(Matrix x, File file) throws IOException {
		if (!file.exists() && !file.createNewFile()) {
			throw new RuntimeException("Could not create file (but also no exception ...)");
		}
		PrintWriter pw = new PrintWriter(Buffer.newOutputStream(file));
		int numRows = x.getNumRows();
		int numColumns = x.getNumColumns();
		for (int i = 0; i < numRows; ++i) {
			for (int j = 0; j < numColumns; ++j) {
				float f = x.get(i,j);
				if (Float.isNaN(f)) {
					continue;
				}
				if (Math.abs(f)< 0.000000001f) {
					// strip 0s
					continue;
				}
				pw.println(i + "\t" + j + "\t" + f);
			}

		}
		pw.close();
	}

	public static void writeSparse(IntMatrix x, File file) throws IOException {
		if (!file.exists() && !file.createNewFile()) {
			System.err.println("Could not create file (but also no exception ...)");
		}
		PrintWriter pw = new PrintWriter(Buffer.newOutputStream(file));
		int numRows = x.getNumRows();
		int numColumns = x.getNumColumns();
		for (int i = 0; i < numRows; ++i) {
			for (int j = 0; j < numColumns; ++j) {
				int f = x.get(i,j);

				if (f==0) {
					// strip 0s
					continue;
				}
				pw.println(i + "\t" + j + "\t" + f);
			}

		}
		pw.close();
	}

	public static void writeSparseLibSVM(Matrix data, Vector labels, File fn) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Buffer.newOutputStream(fn)));
		for (int i = 0; i < data.getNumRows(); ++i) {
			out.write(labels.get(i)+"");
			for (int j = 0; j < data.getNumColumns(); ++j) {
				float f = data.get(i,j);
				if (Float.isNaN(f)) {
					continue;
				}
				if (Math.abs(f)< 0.000000001f) {
					// strip 0s
					continue;
				}
				out.write(" "+(j+1)+":"+f) ;
			}
			out.write("\n");
		}
		out.close();
	}



	public static void writeAsInts(Matrix x, File fn) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Buffer.newOutputStream(fn)));
		int numRows = x.getNumRows();
		int numColumns = x.getNumColumns();
		out.write(numRows + META_DELIMITER + numColumns);
		out.newLine();
		for (int i = 0; i < numRows; ++i) {
			out.write("" + (int) x.get(i, 0));
			for (int j = 1; j < numColumns; ++j)
				out.write(VALUES_DELIMITER + (int) x.get(i, j));
			out.newLine();
		}
		out.close();
	}

	// hack: writes first value as int, remaining as floats
	public static void writeAsInts2(Matrix x, File fn) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Buffer.newOutputStream(fn)));
		int numRows = x.getNumRows();
		int numColumns = x.getNumColumns();
		out.write(numRows + META_DELIMITER + numColumns);
		out.newLine();
		for (int i = 0; i < numRows; ++i) {
			out.write("" + (int) x.get(i, 0));
			for (int j = 1; j < numColumns; ++j)
				out.write(VALUES_DELIMITER + x.get(i, j));
			out.newLine();
		}
		out.close();
	}


	public static void write(IntMatrix x, File f) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Buffer.newOutputStream(f)));

		int numRows = x.getNumRows();
		int numColumns = x.getNumColumns();
		out.write(numRows + META_DELIMITER + numColumns);
		out.newLine();
		for (int i = 0; i < numRows; ++i) {
			out.write("" + x.get(i, 0));
			for (int j = 1; j < numColumns; ++j)
				out.write(VALUES_DELIMITER + x.get(i, j));
			out.newLine();
		}
		out.close();
	}

	public static void write(Matrix x, File fn) throws IOException {
		write(x,fn,true);
	}

	/**
	 * TODO: Use a WriterConfig instead (to be done)
	 * 
	 * @param x
	 * @param fn
	 * @param header
	 * @throws IOException
	 */
	@Deprecated
	public static void write(Matrix x, File fn, boolean header) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Buffer.newOutputStream(fn)));
		if (header) {
			out.write(x.getNumRows() + META_DELIMITER + x.getNumColumns());
			out.newLine();
		}
		for (int i = 0; i < x.getNumRows(); ++i) {
			out.write("" + x.get(i, 0));
			for (int j = 1; j < x.getNumColumns(); ++j)
				out.write(VALUES_DELIMITER + x.get(i, j));
			out.newLine();
		}
		out.close();
	}




	public static void writeAnnotated(AnnotatedMatrix x, File fn) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Buffer.newOutputStream(fn)));
		Collection<String> annotationKeys = x.getAnnotationKeys();
		int numRows = x.getNumRows();
		int numColumns = x.getNumColumns();
		out.write(numRows + "\t" + numColumns + "\t" + annotationKeys.size());
		out.newLine();
		for (String annotation : annotationKeys) {
			out.write("#" + annotation + "=" + x.getAnnotation(annotation));
			out.newLine();
		}
		for (int i = 0; i < numRows; ++i) {
			out.write("" + x.get(i, 0));
			for (int j = 1; j < numColumns; ++j)
				out.write("\t" + x.get(i, j));
			out.newLine();
		}
		out.close();
	}


	public static void writeBinary(Matrix x, File fn) throws IOException {
		writeBinary(x, fn, WRITE_BINARY_VERSION);
	}

	public static void writeBinary(Matrix x, File fn, byte version) throws IOException {

		//		DataOutputStream dos = null;
		//		try {
		//			dos= new DataOutputStream(Buffer.newOutputStream(fn));
		OutputStream dos = null;
		try {

			switch (version) {
			case BINARY_UNCOMPRESSED:
				dos= new DataOutputStream(Buffer.newOutputStream(fn));
				writeBinary0(x, (DataOutputStream) dos);
				break;
			case BINARY_COMPRESSED:
				// first byte: Version string.
				dos = Buffer.newOutputStream(fn);
				dos.write((byte)0x01);

				GZIPOutputStream out = new GZIPOutputStream(dos);

				writeBinary1(x,out);
				break;
			default:
				throw new RuntimeException("Inidentified version for writing. Are you using the most recent library? Max supported: " + WRITE_BINARY_VERSION);
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (dos != null)
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}



	public static void writeIntBinary(IntMatrix x, File fn, byte version) throws IOException {

		OutputStream dos = null;
		try {

			switch (version) {
			case BINARY_UNCOMPRESSED:
				dos= new DataOutputStream(Buffer.newOutputStream(fn));
				writeIntBinary0(x, (DataOutputStream) dos);
				break;
			case BINARY_COMPRESSED:
				// first byte: Version string.
				dos = Buffer.newOutputStream(fn);
				dos.write((byte)0x01);

				GZIPOutputStream out = new GZIPOutputStream(dos);
				writeIntBinary1(x,out);
				break;
			default:
				throw new RuntimeException("Inidentified version for writing. Are you using the most recent library? Max supported: " + WRITE_BINARY_VERSION);
			}


		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (dos != null)
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}



	private static void writeBinary1(Matrix x, OutputStream out) throws IOException {

		int numRows = x.getNumRows();
		int numColumns = x.getNumColumns();

		writeInt(out, numRows);
		writeInt(out, numColumns);

		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numColumns; ++j)
				writeFloat(out, x.get(i, j));

		out.flush();
		out.close();
	}


	private static void writeIntBinary1(IntMatrix x, OutputStream out) throws IOException {

		int numRows = x.getNumRows();
		int numColumns = x.getNumColumns();

		writeInt(out, numRows);
		writeInt(out, numColumns);

		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numColumns; ++j)
				writeInt(out, x.get(i, j));

		out.flush();
		out.close();
	}

	private final static byte[] WRITE_INT_BUFFER = new byte[4];
	private static final void writeInt(final OutputStream out, final int value)
			throws IOException {

		WRITE_INT_BUFFER[0] = (byte) ((value >>> 24) & 0xFF);
		WRITE_INT_BUFFER[1] = (byte) ((value >>> 16) & 0xFF);
		WRITE_INT_BUFFER[2] = (byte) ((value >>>  8) & 0xFF);
		WRITE_INT_BUFFER[3] = (byte) ((value >>>  0) & 0xFF);
		out.write(WRITE_INT_BUFFER);

		//		out.write((value >>> 24) & 0xFF);
		//        out.write((value >>> 16) & 0xFF);
		//        out.write((value >>>  8) & 0xFF);
		//        out.write((value >>>  0) & 0xFF);
	}

	private static void writeFloat(OutputStream out, float f) throws IOException {
		writeInt(out, Float.floatToIntBits(f));
	}

	private static void writeBinary0(Matrix x, DataOutputStream dos) throws IOException {
		// first byte: Version string.
		dos.writeByte((byte)0x00);

		// second, third values: numRows, numCols
		dos.writeInt(x.getNumRows());
		dos.writeInt(x.getNumColumns());

		for (int i = 0; i < x.getNumRows(); ++i)
			for (int j = 0; j < x.getNumColumns(); ++j)
				dos.writeFloat(x.get(i, j));

		dos.flush();
	}


	private static void writeIntBinary0(IntMatrix x, DataOutputStream dos) throws IOException {
		// first byte: Version string.
		dos.writeByte((byte)0x00);

		// second, third values: numRows, numCols
		dos.writeInt(x.getNumRows());
		dos.writeInt(x.getNumColumns());

		for (int i = 0; i < x.getNumRows(); ++i)
			for (int j = 0; j < x.getNumColumns(); ++j)
				dos.writeInt(x.get(i, j));

		dos.flush();
	}

	/**
	 * @param fn the target file (name)
	 * @param cdomain the domain identifier for the last column (assumed to be the class), e.g. NUMERIC, or a WEKA nominal String.
	 * @param showProgress counter on command line
	 * @throws IOException on error
	 */
	public static void writeArff(File fn, Matrix m) throws IOException {
		writeArff(fn, m, "NUMERIC", 10000);
	}

	/**
	 * @param fn the target file (name)
	 * @param cdomain the domain identifier for the last column (assumed to be the class), e.g. NUMERIC, or a WEKA nominal String.
	 * @param showProgress counter on command line
	 * @throws IOException on error
	 */
	public static void writeArff(File fn, Matrix m, int showProgress) throws IOException {
		writeArff(fn, m, "NUMERIC", showProgress);
	}

	public static void writeArff(File fn, Matrix m, String cdomain,  int showProgress) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Buffer.newOutputStream(fn)));

		int numRows = m.getNumRows();
		int numColumns = m.getNumColumns();
		out.write("@RELATION matrix \n");
		for (int i=0;i<numColumns-1;i++)
			out.write("@ATTRIBUTE attr"+i+" NUMERIC \n");

		out.write("@ATTRIBUTE class "+cdomain+" \n");

		out.write("@DATA \n");
		for (int i = 0; i < numRows; ++i) {
			if ( (i+1) % showProgress == 0)
				System.out.print(".");
			for (int j = 0; j < numColumns; ++j)
				out.write(m.get(i, j)+((j!=numColumns-1)?",":"\n"));
		}
		out.close();
	}


	/**
	 * NOTE: no special handling of missing values is implemented. Missing values need to be implemented/taken care in the encoder!
	 * 
	 * @param fn
	 * @param m
	 * @param enc
	 * @param showProgress
	 * @throws IOException
	 */
	public static void writeArff(File fn, Matrix m, ArffEncoder enc,  int showProgress) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Buffer.newOutputStream(fn)));
		out.write("@RELATION matrix \n");
		boolean ismll = enc instanceof IsmllArffEncoder;

		for (int i=0;i<m.getNumColumns();i++) {
			String attr;
			if (ismll)
				attr=ArffEncoderHelper.getWekaTypeDescription(enc, i, ((IsmllArffEncoderMapper)enc).getMap(i));
			else {
				attr=ArffEncoderHelper.getWekaTypeDescription(enc, i, m);
			}
			String name = enc.getAttributeName(i);
			out.write("@ATTRIBUTE " + name + " "  + attr + " \n");
		}

		out.write("@DATA \n");
		for (int i = 0; i < m.getNumRows(); ++i) {
			if ( (i+1) % showProgress == 0) System.out.print(".");
			for (int j = 0; j < m.getNumColumns(); ++j) {
				String encode = enc.encode(j, m.get(i, j));
				out.write(encode+((j!=m.getNumColumns()-1)?",":"\n"));
			}
		}
		out.close();
	}



	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	public static Matrix read(File f, FileType type, int progress) throws IOException {
		if (f == null) throw new IOException("File to read from is null!");
		if (type==null) {
			logger.info("Trying autosensind the file type for " + f + " ...");
			for (FileType currenttype : FileType.values()) {
				logger.debug(" ... trying " + type);
				try {
					Matrix ret = read(f,currenttype, progress);
					if (ret != null) return ret;
				} catch (RuntimeException e) {
					logger.error(currenttype + " ... failed.");
					logger.debug(e);
				} catch (Exception e) {
					logger.error(currenttype + " ... failed.");
					logger.debug(e);
				}
			}
			logger.warn("Unable to autosense file format. Returning null (expect a crash soon...)");
			return null;
		}


		if (progress<=0)progress=(int) (f.length()/10);
		switch (type) {
		case Binary:
			return readBinary(f);
		case Ismll:
			return readDense(f, progress);
		case AnnotatedIsmll:
			return readAnnotatedDense(f);
		case Arff:
			return readWeka(f, progress).data;
		case Csv:
			return readCsv(f, progress);
		default:
			throw new RuntimeException("Unknown type");
		}

	}

	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	private static Matrix readCsv(File f, int progress) throws IOException {
		return readCsv(Buffer.newInputStream(f), progress);
	}

	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	public static Matrix read(InputStream is, FileType type, int progress) throws IOException {
		switch (type) {
		case Binary:
			return readBinary(is, 8192);
		case Ismll:
			return readDense(is, progress);
		case Arff:
			return readWeka(is, progress).data;
		case Csv:
			return readCsv(is, progress);
		default:
			throw new RuntimeException("Unknown type");
		}
	}

	/**
	 * TODO: Move this to some kind of "convert" method, to dis-confuse "read" and "parse"
	 */
	public static Matrix convert(String str, FileType type, int progress) throws IOException {

		ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
		Matrix ret = read(bais, type, progress);
		bais.close();
		return ret;
	}

	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	private static Matrix readCsv(InputStream is, int progress) throws IOException {
		ReaderConfig rc = new ReaderConfig();
		rc.progressTicker=progress;
		rc.skipLines=1; // header
		rc.autodetectFormat=true;
		return readCsv(is, rc);
	}

	private static Matrix readCsv(InputStream is, ReaderConfig rc) throws IOException {

		class DT{
			DefaultMatrix ret;
		}
		final DT d = new DT();
		readDense(is, rc, new MatrixCallback() {

			@Override
			public void setField(int row, int col, String string) {
				d.ret.set(row, col, Float.parseFloat(string));
			}

			@Override
			public void meta(int numRows, int numColumns) {
				d.ret = new DefaultMatrix(numRows, numColumns);
			}
		});

		return d.ret;
	}

	public static byte WRITE_BINARY_VERSION = 0x00;

	public static byte READ_BINARY_VERSION_SUPPORTED = 0x01;


	public static Matrix readBinary(File f) throws IOException  {
		int bufferSize = Buffer.getBufferSize(f);
		InputStream fis = Buffer.newInputStream(f);

		Matrix readBinary;
		try {
			readBinary = readBinary(fis, bufferSize);
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return readBinary;
	}



	public static IntMatrix readIntBinary(File f) throws IOException  {
		int bufferSize = Buffer.getBufferSize(f);
		InputStream fis = Buffer.newInputStream(f);

		IntMatrix readBinary;
		try {
			readBinary = readIntBinary(fis, bufferSize);
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return readBinary;
	}


	public static IntMatrix readIntBinary(InputStream fis, int bufferSize) throws IOException  {
		if (!(fis instanceof BufferedInputStream))
			fis = new BufferedInputStream(fis, bufferSize);
		DataInputStream dis = new DataInputStream(fis);

		byte version;
		try {
			version = dis.readByte();
		} catch (IOException e1) {
			throw e1;
		}
		if (version > READ_BINARY_VERSION_SUPPORTED) {
			throw new RuntimeException("Version flag in input file is newer than the supported one. Please update the library!");
		}
		long start = System.nanoTime();

		DefaultIntMatrix m = null;
		switch (version) {
		case 0x00: // initial version
			try {
				int numRows = dis.readInt();
				int numCols = dis.readInt();
				m = new DefaultIntMatrix(numRows, numCols);

				int currentRow = 0;
				int currentCol = 0;
				int value;
				while(true) {
					value = dis.readInt();
					m.set(currentRow, currentCol, value);
					currentCol++;
					if (currentCol >= numCols) {
						currentRow++;
						currentCol=0;
					}
				}
			} catch (EOFException e) {
				// finished reading
			} catch (IOException e ) {
				e.printStackTrace();
				throw e;
			}

			break;
		case 0x01: // basic compression.
			

			try (GZIPInputStream is = new GZIPInputStream(dis)) {
				int numRows = readInt(is);
				int numCols = readInt(is);
				m = new DefaultIntMatrix(numRows, numCols);

				int currentRow = 0;
				int currentCol = 0;
				int value;
				while(true) {
					value = readInt(is);

					m.set(currentRow, currentCol, value);
					currentCol++;
					if (currentCol >= numCols) {
						currentRow++;
						currentCol=0;
					}
				}
			} catch (EOFException e) {
				//				e.printStackTrace();
				// finished reading
			} catch (IOException e ) {
				e.printStackTrace();
				throw e;
			}
			break;
		default:
			throw new RuntimeException("Strange. Version in file does not exceed the supported ones, and the version is still not supported ...");

		}

		long end = System.nanoTime();
		if (debug)
			System.out.println("Loaded from binary input stream in " + (end-start) + " nanoseconds.");

		assert(m!=null);
		if (m == null)
			throw new IOException("Format was recognized, but no matrix was read. File content error??");

		return m;
	}





	public static Matrix readBinary(InputStream fis, int bufferSize) throws IOException  {
		if (!(fis instanceof BufferedInputStream))
			fis = new BufferedInputStream(fis, bufferSize);
		DataInputStream dis = new DataInputStream(fis);

		byte version;
		try {
			version = dis.readByte();
		} catch (IOException e1) {
			throw e1;
		}
		if (version > READ_BINARY_VERSION_SUPPORTED) {
			throw new RuntimeException("Version flag in input file is newer than the supported one. Please update the library!");
		}
		long start = System.nanoTime();

		DefaultMatrix m = null;
		switch (version) {
		case 0x00: // initial version
			try {
				int numRows = dis.readInt();
				int numCols = dis.readInt();
				m = new DefaultMatrix(numRows, numCols);

				int currentRow = 0;
				int currentCol = 0;
				float value;
				while(true) {
					value = dis.readFloat();
					m.set(currentRow, currentCol, value);
					currentCol++;
					if (currentCol >= numCols) {
						currentRow++;
						currentCol=0;
					}
				}
			} catch (EOFException e) {
				// finished reading
			} catch (IOException e ) {
				e.printStackTrace();
				throw e;
			}

			break;
		case 0x01: // basic compression.
			

			try (GZIPInputStream is = new GZIPInputStream(dis)){
				int numRows = readInt(is);
				int numCols = readInt(is);
				m = new DefaultMatrix(numRows, numCols);

				int currentRow = 0;
				int currentCol = 0;
				float value;
				while(true) {
					value = readFloat(is);

					m.set(currentRow, currentCol, value);
					currentCol++;
					if (currentCol >= numCols) {
						currentRow++;
						currentCol=0;
					}
				}
			} catch (EOFException e) {
				//				e.printStackTrace();
				// finished reading
			} catch (IOException e ) {
				e.printStackTrace();
				throw e;
			} 
			break;
		default:
			throw new RuntimeException("Strange. Version in file does not exceed the supported ones, and the version is still not supported ...");

		}

		long end = System.nanoTime();
		if (debug)
			System.out.println("Loaded from binary input stream in " + (end-start) + " nanoseconds.");

		assert(m!=null);
		if (m == null)
			throw new IOException("Format was recognized, but no matrix was read. File content error??");

		return m;
	}


	private static float readFloat(InputStream is) throws IOException {
		return Float.intBitsToFloat(readInt(is));
	}

	private static int readInt(InputStream in) throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	/**
	 * reads sparse format without format file (to be used for dense implemetnations, since index-positions, locations, etc. are not available)
	 * 
	 * just iterates through the tuples; not using the format file...
	 * 
	 * @param fn
	 * @param cb
	 * @throws FileNotFoundException
	 */
	public static void readSparse(File fn, MatrixCallback cb) throws FileNotFoundException {
		// 1. pass: compute ranges of row index and column index:
		BufferedReader in = new BufferedReader(new InputStreamReader(Buffer.newInputStream(fn)));

		Scanner sc = new Scanner(in);
		int numTriples = sc.nextInt();
		int numTripleColumns = sc.nextInt();
		if (numTripleColumns != 3) {
			System.err.println("Sparse triple representation should have 3 columns, but got " + numTripleColumns);
			System.exit(-1);
		}
		if (numTriples == 0) {
			System.err.println("Sparse triple representation with 0 triples.");
			System.exit(-1);
		}
		int minRow = sc.nextInt(), maxRow = minRow;
		int minColumn = sc.nextInt(), maxColumn = minColumn;
		float value = sc.nextFloat();
		for (int i = 1; i < numTriples; ++i) {
			int row = sc.nextInt();
			if (row < minRow)
				minRow = row;
			else if (row > maxRow)
				maxRow = row;
			int col = sc.nextInt();
			if (col < minColumn)
				minColumn = col;
			else if (col > maxColumn)
				maxColumn = col;
			value = sc.nextFloat();
		}
		sc.close();
		try {
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		int numRows = maxRow-minRow+1, numColumns = maxColumn-minColumn+1;
		//		DefaultMatrix matrix = new DefaultMatrix(numRows, numColumns);
		cb.meta(numRows, numColumns);

		// 2. pass: read the data.
		in = new BufferedReader(new InputStreamReader(Buffer.newInputStream(fn)));
		sc = new Scanner(in);
		numTriples = sc.nextInt();
		numTripleColumns = sc.nextInt();
		for (int i = 0; i < numTriples; ++i) {
			int row = sc.nextInt();
			int col = sc.nextInt();
			value = sc.nextFloat();
			cb.setField(row-minRow, col-minColumn, value + "");
		}
		sc.close();
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	public static void readDense(InputStream fis, int showProgress, MatrixCallback c) throws IOException {
		readDense(fis, showProgress, 8192, c);
	}

	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	public static void readDense(InputStream fis, int showProgress, int bufferSize, MatrixCallback c) throws IOException {
		ReaderConfig rc = new ReaderConfig();
		rc.progressTicker=showProgress;
		rc.bufferSize=bufferSize;
		readDense(fis, rc, c);
	}

	public static void readDense(InputStream fis, ReaderConfig conf, MatrixCallback c) throws IOException {
		int showProgress = conf.progressTicker;
		char fieldSeparator = conf.fieldSeparator;

		long start = System.nanoTime();

		byte[] buffer = new byte[conf.bufferSize];
		StringBuilder sb = new StringBuilder();

		int numRows, numColumns = -1;
		boolean headerRead= false;

		int read;
		int colNr = 0;
		int rowNr = 0;
		boolean previousLinebreak=false;


		if (conf.autodetectFormat) {
			if(!fis.markSupported()) {
				throw new RuntimeException("Autodetection of file format not supported: input stream does not support marking! Maybe easiest solution: Use a BufferedInputStream!");
			}
			conf.numColumns=0;
			conf.numRows=0;
			int maxCol=0;
			fis.mark(Integer.MAX_VALUE);
			logger.info("Scanning file for numLines and numCols...");
			// analyse ...
			while((read = fis.read(buffer))>0 ) {

				for (int i = 0; i < read; i++) {
					byte b = buffer[i];
					boolean lineBreak = b == '\n' || b == '\r';
					if (b==fieldSeparator) {
						// parse value
						colNr++;
						sb.setLength(0);
					}
					else if (lineBreak && !previousLinebreak) {
						// parse value
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
						if (showProgress>0 && rowNr>0 && rowNr % showProgress == 0)
							System.out.println(rowNr);

					}
					if (lineBreak) {
						maxCol=Math.max(colNr, maxCol);
						colNr = 0;
					}
					previousLinebreak=lineBreak;

				}
			} // of while
			conf.numColumns=maxCol;
			if (sb.length()>0)
				rowNr++;
			conf.numRows=rowNr;
			logger.info(conf.numRows-conf.skipLines + " rows (" + conf.skipLines + " skipped)");
			logger.info(conf.numColumns + " columns");

			sb.setLength(0);
			fis.reset();
		}

		if (conf.numRows > 0 && conf.numColumns  > 0) {
			numRows=conf.numRows;
			numColumns=conf.numColumns;
			headerRead=true;
			c.meta(numRows - conf.skipLines, numColumns);
		}

		colNr = 0;
		rowNr = 0;

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

							String line0 = sb.toString().trim();
							String[] line_arr;
							if (line0.contains("\t"))
								line_arr= line0.split("\t");
							else
								line_arr= line0.split(",");
							numRows = Integer.parseInt(line_arr[0].trim());
							if (line_arr.length>1)
								numColumns = Integer.parseInt(line_arr[1].trim());
							else if (conf.numColumns>0)
								numColumns=conf.numColumns;
							else
								throw new RuntimeException("matrix header information contains no column information; and this is neither contained in the ReaderConfig. Can't load file...");
							if (numRows<=conf.skipLines)
								throw new IOException("No data to read: too many skip-lines requested");

							c.meta(numRows-conf.skipLines, numColumns);
							sb.setLength(0);
						}
					} else {
						// header read; parse lines...

						if (b==fieldSeparator) {
							// parse value
							if (rowNr>=conf.skipLines) {
								String str = sb.toString();
								if (str.length()==0 && conf.defaultValue!=null)
									str=conf.defaultValue;
								c.setField(rowNr-conf.skipLines, colNr, str);
							}
							colNr++;
							sb.setLength(0);
						}
						else if (lineBreak && !previousLinebreak) {
							// parse value
							if (rowNr>=conf.skipLines) {
								String str = sb.toString();
								if (str.length()==0 && conf.defaultValue!=null)
									str=conf.defaultValue;
								c.setField(rowNr-conf.skipLines, colNr, str);
							}

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
							if (showProgress > 0 && rowNr>0 && rowNr % showProgress == 0)
								System.out.println(rowNr);

						}
						if (lineBreak)
							colNr = 0;
					}
					previousLinebreak=lineBreak;

				}
			}
			if ( sb.length()>0) {
				if (rowNr>=conf.skipLines)
					c.setField(rowNr-conf.skipLines, colNr, sb.toString());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		long end = System.nanoTime();
		if (debug) {
			System.out.println("Loaded from input stream in " + (end-start) + " nanoseconds.");
			logger.debug("Loaded from input stream in " + (end-start) + " nanoseconds.");
		}
	}

	public static void readAnnotatedMatrix(InputStream fis, ReaderConfig conf, AnnotatedMatrixCallback c) throws IOException {
		char fieldSeparator = conf.fieldSeparator;

		long start = System.nanoTime();

		byte[] buffer = new byte[conf.bufferSize];
		StringBuilder sb = new StringBuilder();

		int numRows, numColumns, numAnnotations = -1;
		boolean headerRead= false;

		int read;
		int colNr = 0;
		int rowNr = 0;
		boolean previousLinebreak=false;
		boolean annotationLine = false;
		boolean resetAnnotationLine = false;

		if (conf.autodetectFormat) throw new RuntimeException("Autodetection of file type not (yet) supported for annotated matrices.");

		colNr = 0;
		rowNr = 0;

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

							String line0 = sb.toString().trim();
							String[] line_arr;
							line_arr= line0.split("\t");
							if (line_arr.length!=3) throw new RuntimeException("First line of an annotated matrix needs to be exactly 3 entries, delimited by tab.");
							numRows = Integer.parseInt(line_arr[0].trim());
							numColumns = Integer.parseInt(line_arr[1].trim());
							numAnnotations=Integer.parseInt(line_arr[2].trim());

							if (numRows<=conf.skipLines && numRows>0)
								throw new IOException("No data to read: too many skip-lines requested");
							if (numRows==0) {
								logger.warn("File with 0 rows");
							}

							c.meta(numRows-conf.skipLines, numColumns);
							c.numAnnotations(numAnnotations);
							sb.setLength(0);
						}
					} else {
						// header read; parse lines...

						if (b==fieldSeparator && !annotationLine) {
							// parse value
							if (rowNr>=conf.skipLines) {
								String str = sb.toString();
								if (str.length()==0 && conf.defaultValue!=null)
									str=conf.defaultValue;
								c.setField(rowNr-conf.skipLines, colNr, str);
							}
							colNr++;
							sb.setLength(0);
						}
						else if (lineBreak && !previousLinebreak) {
							// parse value
							if (!annotationLine) {
								if (rowNr>=conf.skipLines) {
									String str = sb.toString();
									if (str.length()==0 && conf.defaultValue!=null)
										str=conf.defaultValue;
									c.setField(rowNr-conf.skipLines, colNr, str);
								}

								colNr++;
								sb.setLength(0);
							} else {
								// TODO:
								c.annotation(sb.toString());
								sb.setLength(0);
								resetAnnotationLine=true;
								annotationLine=false;
							}
						}
						else {
							if (previousLinebreak) {
								// determine annotation line
								annotationLine = b == '#';
							}
							// trim whitespace
							if (b != ' ' && !lineBreak)
								sb.append((char)b);
						}

						if (lineBreak && colNr>0 && !annotationLine)
							rowNr++;

						if (lineBreak && !annotationLine)
							colNr = 0;

						if (resetAnnotationLine) {
							annotationLine=false;
							resetAnnotationLine=false;
						}
					}
					previousLinebreak=lineBreak;
				}
			}
			if ( sb.length()>0) {
				if (rowNr>=conf.skipLines)
					c.setField(rowNr-conf.skipLines, colNr, sb.toString());
			}
		} catch (IOException e1) {
			logger.error("Error file parsing annotated Matrix Stream.", e1);
			return;
		}

		long end = System.nanoTime();
		if (debug) {
			System.out.println("Loaded from input stream in " + (end-start) + " nanoseconds.");
			logger.debug("Loaded from input stream in " + (end-start) + " nanoseconds.");
		}
	}
	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	public static Matrix readDense(File f, int showProgress)
			throws IOException {

		InputStream fis = null ;
		try {
			fis= Buffer.newInputStream(f);
			return readDense(fis, 10000);

		} finally {
			if (fis != null)
				fis.close();
		}

	}
	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	public static Matrix readDense(InputStream is, int showProgress) throws IOException {
		DefaultMatrixParser mc = new DefaultMatrixParser();
		Matrices.readDense(is, showProgress, mc);
		return mc.m;
	}

	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	public static Matrix readDense(InputStream is, int showProgress, int bufferSize) throws IOException {
		DefaultMatrixParser mc = new DefaultMatrixParser();
		Matrices.readDense(is, showProgress, bufferSize, mc);
		return mc.m;
	}

	public static AnnotatedMatrix readAnnotatedDense(InputStream is) throws IOException {
		DefaultAnnotatedMatrixParser mc = new DefaultAnnotatedMatrixParser();
		ReaderConfig rc = new ReaderConfig();
		rc.fieldSeparator='\t';
		Matrices.readAnnotatedMatrix(is, rc, mc);
		return mc.m;
	}



	public static AnnotatedMatrix readAnnotatedDense(File in) throws IOException {
		InputStream newInputStream = Buffer.newInputStream(in);

		try{
			return readAnnotatedDense(newInputStream);
		} finally {
			newInputStream.close();
		}
	}

	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	public static void readDense(File fn, int showProgress, MatrixCallback c) throws IOException {

		int bufferSize = Buffer.getBufferSize(fn);
		InputStream fis = Buffer.newInputStream(fn);

		long start = System.nanoTime();

		try {
			readDense(fis, showProgress, bufferSize, c);
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long end = System.nanoTime();
		if (debug)
			System.out.println("Loaded " + fn + " fast        in " + (end-start) + " nanoseconds.");
	}

	public static IsmllArffDataset readWeka(File f) throws IOException {
		return readWeka(f, 1000);
	}

	public static Matrix readWeka0(File f) throws IOException {
		return readWeka(f).data;
	}

	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	public static Matrix readWeka0(File f, int progress) throws IOException {
		return readWeka(f, progress).data;
	}

	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	public static IsmllArffDataset readWeka(File f, int progress) throws IOException {

		InputStream is = Buffer.newInputStream(f);
		try {
			IsmllArffDataset readWeka = readWeka(is, progress, f.length());
			if(readWeka.data.getNumColumns()<0 || readWeka.data.getNumRows()<0) {
				throw new RuntimeException("Matrix dimension implausible (" + readWeka.data.getNumRows() + "x" + readWeka.data.getNumColumns() + ") !");
			}
			return readWeka;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}
	}

	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	public static ArffDataset readWeka(InputStream fis, int progress) throws IOException {
		return readWeka(fis, progress, -1);
	}

	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	public static Matrix readWeka0(InputStream fis, int progress) throws IOException {
		return readWeka(fis, progress).data;
	}

	/**
	 * TODO: Use ReaderConfig, instead of progress
	 */
	@Deprecated
	private static IsmllArffDataset readWeka(InputStream is, int progress, long filesize) throws IOException {
		IsmllArffEncoder enc = new IsmllArffEncoder();

		int currentRow=0;
		String line = null;
		String datasetname=null;
		int numAttributes=0;
		int currentAttribute=0;
		int[] attributeIndizes = null;
		TreeMap<Integer, Integer> attribute2Index = new TreeMap<Integer, Integer>();
		Pattern p = Pattern.compile(",");

		TreeMap<Integer, Map<String, Integer>> nonNumericalMappingMap = new TreeMap<Integer, Map<String,Integer>>();

		IsmllArffDataset ret = null;
		boolean readData = false;
		StringBuilder sb = new StringBuilder();
		int content;
		boolean skipUntilLinebreak=false;
		boolean lastWasLinebreak=false; // handling Windows/Unix line breaks
		boolean lastWasWhitespace=false;

		while (!readData && (content=is.read())>=0) { // meaning: read header
			// fetch next line from file, count bytes read

			switch ((byte)content) {
			case '%':
				skipUntilLinebreak=true;
				lastWasWhitespace=false;
				continue;
			case '\n':
			case '\r':
				skipUntilLinebreak=false;
				lastWasLinebreak=true;
				lastWasWhitespace=false;
				continue;
			case ' ':
			case '\t': // replace tabs by whitespace
				if (!lastWasWhitespace && !skipUntilLinebreak)
					sb.append(' ');
				lastWasWhitespace=true;
				continue;
			default:
				if (lastWasLinebreak && sb.length()>0){
					line = sb.toString();
					sb.setLength(0);
				}
				if (!skipUntilLinebreak)
					sb.append((char)content);
				lastWasLinebreak=false;
				lastWasWhitespace=false;
			}
			if (skipUntilLinebreak)
				continue;
			if (line == null)
				continue;

			line = line.trim();
			if ("".equals(line))
				continue;
			logger.debug("Header-line:" + line);
			if (line.startsWith("@")) {
				if (line.length() > "@RELATION".length() &&
						line.substring(1, "@RELATION".length()).equalsIgnoreCase("RELATION")) {
					datasetname=line.substring("@RELATION".length()).trim();
					enc.datasetname=datasetname;
				} else
					if (line.length() > "@ATTRIBUTE".length() &&
							line.substring(1, "@ATTRIBUTE".length()).equalsIgnoreCase("ATTRIBUTE")) {
						int attr_name_split = line.indexOf(' ');
						int name_type_split = line.indexOf(' ', attr_name_split+1);
						String attributeName=line.substring(attr_name_split+1, name_type_split);
						String attributeType=line.substring(name_type_split+1);
						enc.setAName(currentAttribute, attributeName);
						enc.setAType(currentAttribute, attributeType);

						if (attributeType.equalsIgnoreCase("NUMERIC") || attributeType.equalsIgnoreCase("REAL")) {
							attribute2Index.put(Integer.valueOf(currentAttribute), Integer.valueOf(numAttributes));
							numAttributes++;
						} else
							if (attributeType.startsWith("{")) {
								// probably nominal attribute type
								Map<String, Integer> nominal2Index = new TreeMap<String, Integer>();

								String contents = attributeType.substring(1, attributeType.length()-1);
								String[] nominals = p.split(contents);
								int idx = 0;
								for (String s : nominals)
									nominal2Index.put(s, Integer.valueOf(idx++));
								logger.info("Nominal mapping for attribute " + attributeName + ": " + nominal2Index.toString());
								nonNumericalMappingMap.put(Integer.valueOf(currentAttribute), nominal2Index);
								attribute2Index.put(Integer.valueOf(currentAttribute), Integer.valueOf(numAttributes));
								numAttributes++;
							} else
								logger.warn("Attribute type " + attributeType + " not (yet) supported!");

						currentAttribute++;
					} else
						if (line.length() >= "@Data".length() &&
						line.substring(1, "@DATA".length()).equalsIgnoreCase("DATA")) {
							readData=true;

						}

				line = null;
			}
		} // of while (reading header)


		/*
		 * 
		 *  determine number of instances:
		 * 
		 */
		logger.info("Start reading instances for " + datasetname);
		boolean useHarddrive=true;
		long expectedFileSize = filesize * 2; // TODO: this is nuts - better check actual free memory in old generation
		if (filesize > 0 && is.markSupported()) {
			long freeMemory = Runtime.getRuntime().freeMemory();
			if (freeMemory<expectedFileSize)
				System.gc();
			if (freeMemory>expectedFileSize)
				useHarddrive=false;
		} else {
			// no file estimate given, forced to use hard drive. SLOW!!
			useHarddrive=true;
		}
		int numBytes = -1;

		int numRows=0;

		if (useHarddrive) {
			File tmpFile = File.createTempFile("Matrices.readweka.tmpfile", "txt");
			tmpFile.deleteOnExit();
			logger.warn("Warning! The whole data section is copied to " + tmpFile);
			logger.warn("Consider increasing your RAM!");

			OutputStream tmpStream = Buffer.newOutputStream(tmpFile);

			boolean previousLinebreak = false;
			byte[] buffer = new byte[128*1024]; // 128 KB-blocks
			while ((numBytes = is.read(buffer))>0) {
				for (int i = 0; i < numBytes; i++) {
					switch (buffer[i]) {
					case '\n':
					case '\r':
						if (!previousLinebreak)
							numRows++;
						previousLinebreak=true;
						break;
					default:
						previousLinebreak=false;
					}
				}
				tmpStream.write(buffer, 0, numBytes);
			}
			if (!previousLinebreak)
				numRows++; // last line contains data
			tmpStream.close();
			is = Buffer.newInputStream(tmpFile);
		} else {
			// use memory.
			logger.debug("Fine! Enough memory available to buffer content.");
			is.mark(Integer.MAX_VALUE);

			int bufferSize = -1;
			if (expectedFileSize > Integer.MAX_VALUE) {
				logger.warn("Huh? Filesize ENOURMOUS - larger than Integer.MAX_VALUE - you don't expect this to fit into memory at all?");
				bufferSize = Integer.MAX_VALUE;
			} else
				bufferSize=(int) expectedFileSize;
			logger.debug("Using buffer size: " + expectedFileSize + "( mem free: " + Runtime.getRuntime().freeMemory() + ")");

			boolean previousLinebreak = false;
			byte[] buffer = new byte[bufferSize];
			while ((numBytes = is.read(buffer))>0) {
				for (int i = 0; i < numBytes; i++) {
					switch (buffer[i]) {
					case '\n':
					case '\r':
						if (!previousLinebreak)
							numRows++;
						previousLinebreak=true;
						break;
					default:
						previousLinebreak=false;
					}
				}
			}
			if (!previousLinebreak)
				numRows++; // last line contains data
			is.reset();
		}


		logger.debug("Number of instances counted: " + numRows);

		for (Entry<Integer, Map<String, Integer>> e : nonNumericalMappingMap.entrySet()) {
			Map<Integer, String> unmap = new TreeMap<Integer, String>();
			for (Entry<String, Integer> e2 : e.getValue().entrySet())
				unmap.put(e2.getValue(), e2.getKey());
			enc.setNominalMap(e.getKey(), unmap);
		}
		DefaultMatrix targetMatrix = new DefaultMatrix(numRows, numAttributes);
		ret = new IsmllArffDataset(targetMatrix, enc);

		// end determining number of instances

		if (attributeIndizes == null) {
			attributeIndizes=new int[currentAttribute];
			for (int i = 0; i < currentAttribute; i++) {
				Integer value = attribute2Index.get(Integer.valueOf(i));
				if (value == null) {
					attributeIndizes[i]=-1;
				} else {
					attributeIndizes[i]=value.intValue();
				}
			}
		}

		/*
		 * read data section
		 */
		int currentParseAttributeIdx = 0;
		byte[] buffer = new byte[128*1024]; // 128 KB blocks
		int cnt = -1;

		while ((cnt=is.read(buffer))>=0) {
			for (int workInBufferPos = 0;workInBufferPos < cnt; workInBufferPos++) {
				content = buffer[workInBufferPos];
				switch (content) {
				case '\n':
				case '\r':
				case ',': // column delimiter for attribute

					if (attributeIndizes[currentParseAttributeIdx]>=0) {
						String valueAsString = sb.toString();
						Integer idx = Integer.valueOf(attributeIndizes[currentParseAttributeIdx]);
						float _val = -1;
						if ("?".equals(valueAsString) && !nonNumericalMappingMap.containsKey(idx)) {
							// missing; strange, but however ...
						} else {
							if (nonNumericalMappingMap.containsKey(idx)) {
								Map<String, Integer> map = nonNumericalMappingMap.get(idx);
								Integer integer = map.get(valueAsString);
								if (integer!=null)
									_val = integer.floatValue();
								else
									_val = -1;  /* missing value indicator */
							} else {
								//								_val = Tools.parseFloat(valueAsString);
								_val = Float.valueOf(valueAsString).floatValue();
							}
						}
						targetMatrix.data[currentRow][attributeIndizes[currentParseAttributeIdx]] = _val;
					}

					sb.setLength(0);
					currentParseAttributeIdx++;
					break;
				case ' ':
				case '\t': // ignore spaces in data section
					lastWasLinebreak=false;
					break;
				default:
					if (lastWasLinebreak && sb.length()>0){
						sb.setLength(0);
					}
					if (!skipUntilLinebreak && content>=0)
						sb.append((char)content);
					lastWasLinebreak=false;
				}

				if (content == '\n' || content == '\r') {
					if (!lastWasLinebreak)
						currentRow++;
					if (!lastWasLinebreak && (currentRow % progress) == 0) {
						System.out.print(".");
					}
					skipUntilLinebreak=false;
					lastWasLinebreak=true;
					currentParseAttributeIdx = 0;
				}

			} // of for (bytes in buffer)
		} // of read data section

		/*
		 * this is weak programming: should be merged/integrated with above switch-case:		 *
		 */
		if (sb.length()>0) {
			if (attributeIndizes[currentParseAttributeIdx]>=0) {
				String valueAsString = sb.toString();
				Integer idx = Integer.valueOf(attributeIndizes[currentParseAttributeIdx]);
				float _val = -1;
				if ("?".equals(valueAsString) && !nonNumericalMappingMap.containsKey(idx)) {
					// missing; strange, but however ...
				} else {
					if (nonNumericalMappingMap.containsKey(idx)) {
						Map<String, Integer> map = nonNumericalMappingMap.get(idx);
						Integer integer = map.get(valueAsString);
						if (integer!=null)
							_val = integer.floatValue();
						else
							_val = -1;  /* missing value indicator */
					} else {
						_val = Float.valueOf(valueAsString).floatValue();
					}
				}
				targetMatrix.data[currentRow][attributeIndizes[currentParseAttributeIdx]] = _val;
			}
		}
		return ret;
	}

	@Deprecated
	public static ArffDataset readWekaSlow(InputStream fis, int progress, long filesize) throws IOException {
		InputStreamReader isr = new InputStreamReader(fis);
		LineNumberReader lnr = new LineNumberReader(isr);

		IsmllArffEncoder enc = new IsmllArffEncoder();

		boolean readData=false;
		int currentRow=0;
		String line = null;
		String datasetname=null;
		int numAttributes=0;
		int currentAttribute=0;
		int[] attributeIndizes = null;
		TreeMap<Integer, Integer> attribute2Index = new TreeMap<Integer, Integer>();
		Pattern p = Pattern.compile(",");

		TreeMap<Integer, Map<String, Integer>> nonNumericalMappingMap = new TreeMap<Integer, Map<String,Integer>>();

		ArffDataset ret = null;
		while ((line = lnr.readLine())!=null) {
			line = line.trim();
			if (line.startsWith("%"))
				continue;
			if (line.equals(""))
				continue;
			if (line.startsWith("@")) {
				if (line.length() > "@RELATION".length() &&
						line.substring(1, "@RELATION".length()).equalsIgnoreCase("RELATION")) {
					datasetname=line.substring("@RELATION ".length());
					enc.datasetname=datasetname;
				} else
					if (line.length() > "@ATTRIBUTE".length() &&
							line.substring(1, "@ATTRIBUTE".length()).equalsIgnoreCase("ATTRIBUTE")) {
						String attributeType=line.substring(line.lastIndexOf(" ")+1);
						String attributeName=line.substring(line.indexOf(' '), line.lastIndexOf(" ")+1);
						enc.setAName(currentAttribute, attributeName);
						enc.setAType(currentAttribute, attributeType);

						if (attributeType.equalsIgnoreCase("NUMERIC")) {
							attribute2Index.put(Integer.valueOf(currentAttribute), Integer.valueOf(numAttributes));
							numAttributes++;
						} else
							if (attributeType.startsWith("{")) {
								// probably nominal attribute type
								Map<String, Integer> nominal2Index = new TreeMap<String, Integer>();

								String contents = attributeType.substring(1, attributeType.length()-1);
								String[] nominals = p.split(contents);
								int idx = 0;
								for (String s : nominals)
									nominal2Index.put(s, Integer.valueOf(idx++));
								System.out.println("Nominal mapping for attribute " + attributeName + ": " + nominal2Index.toString());
								nonNumericalMappingMap.put(Integer.valueOf(currentAttribute), nominal2Index);
								attribute2Index.put(Integer.valueOf(currentAttribute), Integer.valueOf(numAttributes));
								numAttributes++;
							} else
								System.err.println("Attribute type " + attributeType + " not (yet) supported!");

						currentAttribute++;
					} else
						if (line.length() >= "@Data".length() &&
						line.substring(1, "@DATA".length()).equalsIgnoreCase("DATA")) {
							System.out.println("Start reading instances for " + datasetname);
							boolean useHarddrive=true;
							if (filesize > 0) {
								long freeMemory = Runtime.getRuntime().freeMemory();
								long expectedFileSize = (filesize) * 3;
								if (freeMemory<expectedFileSize)
									System.gc();
								if (freeMemory>expectedFileSize)
									useHarddrive=false;
							} else {
								// no file estimate given, forced to use hard drive. SLOW!!
								useHarddrive=true;
							}
							InputStream fis2;
							String tmpLine = null;
							int numRows=0;
							if (useHarddrive) {
								File tmpFile = File.createTempFile("Matrices.readweka.tmpfile", "txt");
								tmpFile.deleteOnExit();
								System.out.println("Warning! Due to Java-Restriction in buffering LineNumberReaders, the whole data section is copied to " + tmpFile);
								System.out.println("Consider increasing your RAM!");
								PrintStream ps =new PrintStream(tmpFile);
								while ((tmpLine = lnr.readLine())!=null) {
									if (tmpLine.trim().equals(""))
										continue;
									numRows++;
									ps.print(tmpLine + "\n");
								}
								ps.close();
								fis2 = new FileInputStream(tmpFile);
							} else {
								// use memory.
								System.out.println("Fine! Enough memory available to buffer content.");

								StringBuffer sb = new StringBuffer();
								while ((tmpLine = lnr.readLine())!=null) {
									if (tmpLine.trim().equals(""))
										continue;
									numRows++;
									sb.append(tmpLine + "\n");
								}
								fis2 = new ByteArrayInputStream(Tools.getBytes(sb.toString()));
							}
							isr = new InputStreamReader(fis2);
							lnr = new LineNumberReader(isr);


							for (Entry<Integer, Map<String, Integer>> e : nonNumericalMappingMap.entrySet()) {
								Map<Integer, String> unmap = new TreeMap<Integer, String>();
								for (Entry<String, Integer> e2 : e.getValue().entrySet())
									unmap.put(e2.getValue(), e2.getKey());
								enc.setNominalMap(e.getKey(), unmap);
							}
							ret = new ArffDataset(new DefaultMatrix(numRows, numAttributes), enc);
							readData=true;
						}

				continue; // end parsing @
			}
			if (readData) {
				assert (ret != null);

				if (attributeIndizes == null) {
					attributeIndizes=new int[currentAttribute];
					for (int i = 0; i < currentAttribute; i++) {
						Integer value = attribute2Index.get(Integer.valueOf(i));
						if (value == null) {
							attributeIndizes[i]=-1;
						} else {
							attributeIndizes[i]=value.intValue();
						}
					}
				}
				String[] attributesAsString = p.split(line);
				for (int i = 0; i < attributesAsString.length; i++) {
					if (attributeIndizes[i]>=0) {
						Integer idx = Integer.valueOf(attributeIndizes[i]);
						if ("?".equals(attributesAsString[i]) && !nonNumericalMappingMap.containsKey(idx)) {
							// missing
							continue;
						}
						if (nonNumericalMappingMap.containsKey(idx)) {
							float _val = -1;
							Map<String, Integer> map = nonNumericalMappingMap.get(idx);
							Integer integer = map.get(attributesAsString[i]);
							if (integer!=null)
								_val = integer.floatValue();
							else
								_val = -1;  /* missing value indicator */
							ret.data.set(currentRow, attributeIndizes[i], _val);
						} else
							ret.data.set(currentRow, attributeIndizes[i], (float)Double.parseDouble(attributesAsString[i]));
					}
				}
				currentRow++;
				if ((currentRow % progress) == 0) {
					System.out.print(".");
				}
			} // of read data section
		}
		return ret;
	}


	public static Matrix[] split(Matrix m,
			int num_splits) {
		Matrix[] splits = new Matrix[num_splits];
		for (int i=0;i<num_splits;i++) {
			splits[i] = Matrices.subMatrixByRow( m, i*m.getNumRows()/num_splits, (i+1)*m.getNumRows()/num_splits );
		}
		return splits;
	}


	/**
	 * Anbigous name; use views instead.
	 */
	@Deprecated
	public static Matrix subMatrixByRow(Matrix m, int fromRow, int toRow) {
		int row_num_new = toRow-fromRow;
		DefaultMatrix m1 = new DefaultMatrix(row_num_new, m.getNumColumns());
		int k=0;
		for (int i=fromRow;i<toRow;i++) {
			for (int j=0;j<m.getNumColumns();j++)
				m1.set(k, j, m.get(i,j));
			k++;
		}
		return m1;
	}

	/**
	 * Anbigous name; use views instead.
	 */
	@Deprecated
	public static DefaultMatrix putTogether(Matrix m1, Matrix m2) throws Exception {
		if (m1.getNumColumns()!=m2.getNumColumns()) throw new Exception("Matrix dimension mismatch");
		DefaultMatrix m = new DefaultMatrix(m1.getNumRows()+m2.getNumRows(), m1.getNumColumns());

		for (int i=0;i<m1.getNumRows();i++)
			for (int j=0;j<m1.getNumColumns();j++)
				m.set(i, j, m1.get(i,j));

		for (int i=0;i<m2.getNumRows();i++)
			for (int j=0;j<m2.getNumColumns();j++)
				m.set(i+m1.getNumRows(), j, m2.get(i,j));

		return m;
	}

	/**
	 * Anbigous name; use views instead.
	 */
	@Deprecated
	public static Matrix putTogetherByCol(Matrix m1, Matrix m2) throws Exception {
		if (m1.getNumRows()!=m2.getNumRows()) throw new Exception("Matrix dimension mismatch");
		DefaultMatrix m = new DefaultMatrix(m1.getNumRows(), m1.getNumColumns()+m2.getNumColumns());

		for (int i=0;i<m1.getNumRows();i++)
			for (int j=0;j<m1.getNumColumns();j++)
				m.set(i, j, m1.get(i,j));

		for (int i=0;i<m2.getNumRows();i++)
			for (int j=0;j<m2.getNumColumns();j++)
				m.set(i, j+m1.getNumColumns(), m2.get(i,j));

		return m;
	}


	public static void sortByCol(Matrix m, int col) {
		final int col0 = col;

		int numRows = m.getNumRows();
		int numColumns = m.getNumColumns();
		float[][] data = new float[numRows][numColumns];
		for (int i=0;i<numRows;i++)
			for (int j=0;j<numColumns;j++)
				data[i][j] = m.get(i, j);

		java.util.Arrays.sort(data, new Comparator<float[]>(){
			public int compare(float[] row1, float[] row2) {
				return row1[col0]>row2[col0] ? 1 : (row1[col0]<row2[col0] ? -1 : 0);
			}
		});

		for (int i=0;i<numRows;i++)
			for (int j=0;j<numColumns;j++)
				m.set(i, j, data[i][j]);
	}



	/**
	 * be careful: iff you are converting test instances,
	 * 
	 * @param m
	 * @param enc
	 * @return
	 */
	public static Instances wekaInstances(Matrix m, ArffEncoder enc) {
		return wekaInstances(new ArffDataset(m, enc));
	}

	public static Instances wekaInstances(ArffDataset arff) {
		return wekaInstances(arff, false);
	}

	public static Instances wekaInstances(ArffDataset arff, boolean zeroEncodesMissing) {
		int numRows = arff.data.getNumRows();
		int numCols = arff.data.getNumColumns();
		ArrayList<Attribute> attrs = new ArrayList<Attribute>(numCols);
		ArffEncoder encoder = arff.getEncoder();
		boolean ismllEncoder = encoder instanceof IsmllArffEncoderMapper;

		for (int i = 0; i < numCols; i++) {
			Attribute current;

			String name = encoder.getAttributeName(i);
			if (arff.isNominal(i)) {
				List<String> nominalValues;
				if (ismllEncoder)
					nominalValues = new ArrayList<String>(((IsmllArffEncoderMapper)encoder).getMap(i).values());
				else {
					nominalValues = deriveNominals(arff, i);
				}
				current = new Attribute(name, nominalValues);
			} else if (arff.isNumeric(i)) {
				current = new Attribute(name);
			} else {
				logger.warn("Unidentified attribute type for index " + i + " in dataset " + arff.getName());
				continue;
			}
			attrs.add(current);
		}

		Instances ret = new Instances(arff.getName(), attrs, numRows);

		for (int i = 0; i < numRows; i++) {
			Instance in = new DenseInstance(numCols);
			in.setDataset(ret);
			for (int j = 0; j < numCols; j++) {
				if (arff.isNominal(j)) {
					String encode = arff.unmap(j, arff.data.get(i, j));
					if ("?".equals(encode))
						in.setMissing(j);
					else
						in.setValue(j, encode);
				}else if (arff.isNumeric(j)) {
					if (zeroEncodesMissing)
						in.setMissing(j);
					else
						in.setValue(j, arff.data.get(i, j));
				}
			}
			ret.add(in);
		}

		return ret;
	}

	private static List<String> deriveNominals(ArffDataset arff, int column) {
		TreeSet<String> s =new TreeSet<String>();
		ArffEncoder encoder = arff.getEncoder();
		for (int i = 0; i < arff.data.getNumRows(); i++) {
			String encode = encoder.encode(column, arff.data.get(i, column));
			s.add(encode);
		}
		return new ArrayList<String>(s);
	}

	/** Assumes that last column of the matrix is the labels column */
	@Deprecated
	public static weka.core.Instances wekaInstances(Matrix m) throws IOException {
		return wekaInstances(m, "NUMERIC");
	}
	@Deprecated
	public static weka.core.Instances wekaInstances(Matrix m, String cdomain) throws IOException {
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
		for (int i=0;i<m.getNumColumns()-1;i++)
			out.write("@ATTRIBUTE attr"+i+" NUMERIC \n");

		out.write("@ATTRIBUTE class "+cdomain+" \n");

		out.write("@DATA \n");
		for (int i = 0; i < m.getNumRows(); ++i) {
			for (int j = 0; j < m.getNumColumns(); ++j)
				out.write(m.get(i, j)+((j!=m.getNumColumns()-1)?",":"\n"));
		}
		out.close();

		return new weka.core.Instances(out);
		//System.out.println("");
	}



	/**
	 * Code duplication, use Vectors.avg(Matrices.row(mm,row)) instead
	 */
	@Deprecated
	public static float avgRow(Matrix mm, int row, boolean[] mask) {
		float a = 0, c = 0;
		for (int i=0;i<mm.getNumColumns();i++) {
			if (!mask[i]) continue;
			a+=mm.get(row, i);
			c++;
		}
		return ( a/c );
	}

	/**
	 * Code duplication, use Vectors.avg(Matrices.row(mm,row)) instead
	 */
	@Deprecated
	public static float avgRow(Matrix mm, int row) {
		float a = 0, c = 0;
		for (int i=0;i<mm.getNumColumns();i++) {
			a+=mm.get(row, i);
			c++;
		}
		return ( a/c );
	}

	/**
	 * Code duplication, use Vectors.avg(Matrices.row(mm,row)) instead
	 */
	@Deprecated
	public static float avgRow(Matrix mm, int row, DefaultMatrix weights) {
		float a = 0, c = 0;
		for (int i=0;i<mm.getNumColumns();i++) {
			a+=(mm.get(row, i)*weights.get(0, i));
			c+=weights.get(0,i);
		}
		return ( a/c );
	}

	


	/**
	 * Code duplication, use Vectors.avg(Matrices.col(mm,row)) instead
	 */
	@Deprecated
	public static float avgCol(Matrix mm, int col, boolean[] mask) {
		float a = 0, c = 0;
		for (int i=0;i<mm.getNumRows();i++) {
			if (!mask[i]) continue;
			a+=mm.get(i, col);
			c++;
		}
		return ( a/c );
	}

	/**
	 * Code duplication, use Vectors.var(Matrices.col(m,row)) instead
	 */
	@Deprecated
	public static float varCol(Matrix m, int col) {
		float a = 0, c=0;
		double mean2 = avgCol(m,col);
		for (int i=0;i<m.getNumRows();i++) {
			a+=((m.get(i,col)-mean2))*((m.get(i,col)-mean2));
			c++;
		}
		return (float) Math.sqrt( a/c );
	}

	/**
	 * Code duplication, use Vectors.avg(Matrices.col(mm,row)) instead
	 */
	@Deprecated
	public static float avgCol(Matrix mm, int col) {
		float a = 0, c = 0;
		for (int i=0;i<mm.getNumRows();i++) {
			a+=mm.get(i, col);
			c++;
		}
		return ( a/c );
	}

	/**
	 * Code duplication, use Vectors.stddev(Matrices.col(mm,row)) instead
	 */
	@Deprecated
	public static float stdCol(Matrix mm, int col, float avg, boolean[] mask) {
		float a = 0, c=0;
		for (int i=0;i<mm.getNumRows();i++) {
			if (!mask[i]) continue;
			a+=((mm.get(i, col)-avg)*(mm.get(i, col)-avg));
			c++;
		}
		return (float) Math.sqrt( a/c );
	}

	/**
	 * Code duplication, use Vectors.stddev(Matrices.col(mm,col)) instead
	 */
	@Deprecated
	public static float stdCol(Matrix mm, int col, float avg) {
		float a = 0, c=0;
		for (int i=0;i<mm.getNumRows();i++) {
			a+=((mm.get(i, col)-avg)*(mm.get(i, col)-avg));
			c++;
		}
		return (float) Math.sqrt( a/c );
	}

	/**
	 * Code duplication, use Vectors.stddev(Matrices.col(mm,col)) instead
	 */
	@Deprecated
	public static float stdCol(Matrix mm, int col) {
		return stdCol(mm, col, avgCol(mm, col));
	}


	public static Matrix selectColumns(Matrix m, int[] mask) {
		int newNumCols = 0;
		int oldNumCols = m.getNumColumns();
		int oldNumRows = m.getNumRows();

		for (int i=0;i<oldNumCols;i++) {
			if (mask[i]<0) continue;
			newNumCols++;
		}


		DefaultMatrix m1 = new DefaultMatrix(oldNumRows, newNumCols);
		for (int i=0;i<oldNumCols;i++) {
			if (mask[i]<0) continue;
			for (int j=0;j<oldNumRows;j++)
				m1.set(j, mask[i], m.get(j,i));
		}
		return m1;
	}

	public static Matrix selectColumns(Matrix m, boolean[] mask) {
		int num_cols = 0;
		for (int i=0;i<m.getNumColumns();i++) {
			if (!mask[i]) continue;
			num_cols++;
		}


		DefaultMatrix m1 = new DefaultMatrix(m.getNumRows(), num_cols);
		int k=0;
		for (int i=0;i<m.getNumColumns();i++) {
			if (!mask[i]) continue;
			for (int j=0;j<m.getNumRows();j++)
				m1.set(j, k, m.get(j,i));
			k++;
		}
		return m1;
	}
	/**
	 * misleading method name
	 */
	 @Deprecated
	 public static float euclideanDistance(Matrix m0, int rownum, Matrix rows, int rownum2) {
		 float d = 0;
		 for (int i=0;i<m0.getNumColumns();i++) {
			 float d0 = m0.get(rownum,i)-rows.get(rownum2, i);
			 d+= (d0*d0);
		 }
		 return (float)Math.sqrt(d);
	 }

	 public static void increment(Matrix m, int row, int col) {
		 m.set(row, col, m.get(row,col)+1 );
		 //data[row][col]++;
	 }

	 public static DefaultMatrix getConstMatrix(int rows, int cols, float value) {
		 DefaultMatrix m = new DefaultMatrix(rows,cols);
		 for (int i=0;i<rows;i++)
			 for (int j=0;j<cols;j++)
				 m.set(i,j,value);
		 return m;
	 }


	 /**
	  * rename to "rows", use IntVector instead of mask; return view
	  */
	 @Deprecated
	 public static Matrix rows(Matrix m, int[] mask) {
		 int num_rows = 0;
		 for (int i=0;i<m.getNumRows();i++) {
			 if (mask[i]<0) continue;
			 num_rows++;
		 }


		 DefaultMatrix m1 = new DefaultMatrix(num_rows, m.getNumColumns());
		 for (int i=0;i<m.getNumRows();i++) {
			 if (mask[i]<0) continue;
			 for (int j=0;j<m.getNumColumns();j++)
				 m1.set(mask[i], j, m.get(i,j));
		 }
		 return m1;
	 }

	 /**
	  * rename in "rows"; use BitVector instead; return view
	  */
	 @Deprecated
	 public static DefaultMatrix rows(Matrix m, BitVector mask) {
		 Assert.assertTrue(mask.size()==m.getNumRows(), "num rows match");
		 int num_rows = 0;
		 for (int i=0;i<m.getNumRows();i++) {
			 if (!mask.get(i)) continue;
			 num_rows++;
		 }


		 DefaultMatrix m1 = new DefaultMatrix(num_rows, m.getNumColumns());
		 int k=0;
		 for (int i=0;i<m.getNumRows();i++) {
			 if (!mask.get(i)) continue;
			 for (int j=0;j<m.getNumColumns();j++)
				 m1.set(k, j, m.get(i,j));
			 k++;
		 }
		 return m1;
	 }


	 /**
	  * rename to "row"
	  */
	 @Deprecated
	 public static Vector getRowVector(Matrix m, int row) {
		 DefaultVector d = new DefaultVector(m.getNumColumns());
		 for (int i=0;i<m.getNumColumns();i++)
			 d.set(i, m.get(row, i));
		 return d;
	 }


	 public static float[][] asArray(Matrix a) {
		 if (a instanceof DefaultMatrix)
			 return ((DefaultMatrix)a).data;
		 float[][] ret = new float[a.getNumRows()][a.getNumColumns()];
		 for (int i = 0; i< a.getNumRows(); i++)
			 for (int j = 0; j < a.getNumColumns(); j++)
				 ret[i][j]=a.get(i, j);
		 return ret;
	 }


	 public static String toString(Matrix m) {
		 StringBuffer sb = new StringBuffer();

		 int numRows = m.getNumRows();
		 int numColumns = m.getNumColumns();

		 for (int i = 0; i < numRows; i++) {
			 sb.append(i);

			 for (int j = 0; j < numColumns; j++)
				 sb.append("\t" + m.get(i, j));

			 sb.append("\n");
		 }

		 return sb.toString();
	 }
	 
	 public static String toString(IntMatrix m) {
		 StringBuffer sb = new StringBuffer();

		 int numRows = m.getNumRows();
		 int numColumns = m.getNumColumns();

		 for (int i = 0; i < numRows; i++) {
			 sb.append(i);

			 for (int j = 0; j < numColumns; j++)
				 sb.append("\t" + m.get(i, j));

			 sb.append("\n");
		 }

		 return sb.toString();
	 }


	 public static void set(Matrix m, float f) {
		 int numRows = m.getNumRows();
		 for (int r = 0; r < numRows; r++) {
			 Vectors.set(row(m,r), f);
		 }
		 //		int numColumns = m.getNumColumns();
		 //		for (int col= 0; col < numColumns; col++)
		 //			setColumn(m, col, f);
	 }


	 public static Matrix asMatrix(Instances inst) {
		 return Arff2Ismll.convert(inst);
	 }

	 public static ArffDataset asArffDataset(Instances inst) {
		 String string = inst.toString();
		 BufferedInputStream bais = new BufferedInputStream(new ByteArrayInputStream(Tools.getBytes(string)));
		 try {
			 return readWeka(bais, 10000, string.length());
		 } catch (IOException e) {
			 throw new RuntimeException(e);
		 }
	 }

	 public static Matrix asMatrix(ArffDataset arff) {
		 return arff.data;
	 }

	 public static float max(Matrix m) {
		 int numColumns = m.getNumColumns();
		 int numRows = m.getNumRows();
		 float ret = -Float.MAX_VALUE;
		 for (int j = 0; j < numRows; j++)
			 for (int i = 0; i < numColumns; i++) {
				 float b = m.get(j, i);
				 if (!Float.isNaN(b) && b >= ret)
					 ret = b;
				 //					ret = (!Float.isNaN(b) && ret >= b) ? ret : b ;
			 }
		 return ret;
	 }

	 public static float max2(Matrix test) {
		 int numColumns = test.getNumColumns();
		 int numRows = test.getNumRows();
		 float ret = -Float.MAX_VALUE;
		 for (int j = 0; j < numRows; j++)
			 for (int i = 0; i < numColumns; i++) {
				 float b = test.get(j, i);
				 ret = (ret >= b) ? ret : b ;
			 }
		 return ret;
	 }

	 public static int max(IntMatrix test) {
		 int numColumns = test.getNumColumns();
		 int numRows = test.getNumRows();
		 int ret = Integer.MIN_VALUE;
		 for (int j = 0; j < numRows; j++)
			 for (int i = 0; i < numColumns; i++) {
				 int b = test.get(j, i);
				 ret = (ret >= b) ? ret : b ;
			 }
		 return ret;
	 }
	 
	 public static Matrix readDense(InputStream is,
			 ReaderConfig rc) throws IOException {
		 DefaultMatrixParser p = new DefaultMatrixParser();
		 readDense(is, rc, p);
		 return p.m;
	 }

	 public static float min(Matrix m) {
		 int numColumns = m.getNumColumns();
		 int numRows = m.getNumRows();
		 float ret = Float.MAX_VALUE;
		 for (int j = 0; j < numRows; j++)
			 for (int i = 0; i < numColumns; i++) {
				 float b = m.get(j, i);
				 if (!Float.isNaN(b) && b < ret)
					 ret=b;
				 //				ret = (ret <= b) ? ret : b;
			 }
		 return ret;

	 }

	 public static void scale(Matrix use, float newMinValue, float newMaxValue) {
		 float currentMin = min(use);
		 float currentMax = max(use);

		 int numRows = use.getNumRows();
		 int numColumns = use.getNumColumns();

		 float newRange = newMaxValue-newMinValue;
		 float oldRange = currentMax-currentMin;

		 for (int r = 0; r < numRows; r++)
			 for (int c = 0; c < numColumns; c++) {

				 float currentValue = use.get(r,c);
				 float t = (currentValue-currentMin)/oldRange;
				 float newValue = newMinValue + t * newRange;
				 use.set(r, c, newValue);
			 }

	 }

	 public static void mult(Matrix use, float f) {
		 int numRows = use.getNumRows();
		 int numColumns = use.getNumColumns();
		 for (int r = 0; r < numRows; r++)
			 for (int c = 0; c < numColumns; c++)
				 use.set(r, c, f * use.get(r, c));

	 }

	 public final static Matrix rows(final Matrix source, final IntVector rowIndizes,
			 final boolean writes) {
		 return new RowSubsetMatrixView(source, rowIndizes, writes);
	 }

	 public final static Matrix rows(final Matrix input, final BitVector selector, final boolean writes) {
		 return new RowSubsetMatrixView(input, selector);
	 }

	 public final static Vector col(final Matrix in, final int column) {
		 return new ColumnSubsetVectorView(in, column);
	 }

	 public final static Vector row(Matrix in, final int row) {
		 return new RowSubsetVectorView(in, row);
	 }


	 public static IntVector col(final IntMatrix in, final int column) {
		 return new ColumnSubsetIntVectorView(in, column);
	 }

	 /**
	  * @param how HORIZONTAL, to (virtually) swap columns (s.t. in[r][#columns-1]<->in[r][0], ...), VERTICAL, s.t. in[0] <-> in[#rows-1] ...
	  * @param in
	  * @param copy whether or not to return a copy or a projection of the passed Matrix
	  * @return
	  */
	 public static Matrix mirror(Mirror how, Matrix in, boolean copy) {
		 int numColumns = in.getNumColumns();
		 int numRows = in.getNumRows();
		 switch (how) {
		 case HORIZONTAL: {
			 DefaultIntVector viewCol = new DefaultIntVector(numColumns);
			 for (int i = 0; i < numColumns; i++)
				 viewCol.set(i, numColumns-i-1);

			 ColumnSubsetMatrixView m = new ColumnSubsetMatrixView(in, viewCol);
			 if (copy) {
				 return new DefaultMatrix(m);
			 }
			 return m;

		 }
		 case VERTICAL: {
			 DefaultIntVector viewRow = new DefaultIntVector(numRows);
			 for (int i = 0; i < numRows; i++)
				 viewRow.set(i, numRows-i-1);
			 RowSubsetMatrixView m = new RowSubsetMatrixView(in, viewRow);
			 if (copy) {
				 return new DefaultMatrix(m);
			 }
			 return m;

		 }

		 }
		 logger.warn("No projection given. Returning null Matrix");
		 return null;
	 }

	 /**
	  * use Vectors.set(Matrices.row(m,row),data) instead
	  */
	 @Deprecated
	 public static void setRow(Matrix m, int row, Vector data) {
		 if (m.getNumColumns()!=data.size()) throw new RuntimeException("Invalid matrix / vector lengths");
		 for (int c = 0; c < data.size(); c++) {
			 m.set(row, c, data.get(c));

		 }
	 }

	 /**
	  * Name misleading
	  */
	 @Deprecated
	 public static void add(Matrix matrix, int column, int value) {
		 for (int r = 0; r < matrix.getNumRows(); r++)
			 matrix.set(r, column, matrix.get(r, column) + value);
	 }

	 public static void copy(Matrix from, Matrix to) {
		 int numRows = from.getNumRows();
		 int numColumns = from.getNumColumns();
		 for (int r = 0; r < numRows; r++)
			 for (int c = 0; c < numColumns; c++)
				 to.set(r, c, from.get(r, c));

	 }

	 public static Matrix cols(Matrix m, int[] indizes) {
		 return new ColumnSubsetMatrixView(m, indizes);
	 }




}



