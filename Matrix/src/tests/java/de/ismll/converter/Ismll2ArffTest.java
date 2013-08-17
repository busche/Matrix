package de.ismll.converter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import junit.framework.Assert;
import weka.core.Instances;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.MatrixTest;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.io.weka.ArffEncoder;

public class Ismll2ArffTest extends MatrixTest {


	//	@Test
	public void testConvertIsmll2WekaBinary() throws IOException {
		DefaultMatrix createSparseMatrix = createSparseMatrix(10, 40000, 0);
		File tmp = File.createTempFile("ismll2arff", "test");
		File out = File.createTempFile("ismll2arff", "output");
		tmp.deleteOnExit();
		out.deleteOnExit();
		System.out.println("tmp:\t" + tmp);
		System.out.println("out:\t" + out);
		Matrices.writeBinary(createSparseMatrix, tmp);
		Ismll2Arff a = new Ismll2Arff();
		a.setInput(tmp);
		a.setOutput(out);
		a.setBinary(true);
		a.setProgressCounter(10000);
		a.run();
		System.out.println("ISMLL-Size: " + tmp.length());
		System.out.println("ARFF-Size: " + out.length());
		Matrix read = Matrices.readWeka0(out);

		checkSame(createSparseMatrix, read);
	}

	//	@Test
	public void testConvertIsmll2Weka() throws IOException {
		//		DefaultMatrix createSparseMatrix = createSparseMatrix(10, 40000, 0);
		//		File tmp = File.createTempFile("ismll2arff", "test");
		//		File out = File.createTempFile("ismll2arff", "output");
		//		tmp.deleteOnExit();
		//		out.deleteOnExit();
		File tmp= new File("c:\\work\\ismll2arff555329392994766195test");
		File out= new File("c:\\work\\ismll2arff9063164632844804477output");
		System.out.println("tmp:\t" + tmp);
		//		System.out.println("out:\t" + out);
		//		Matrices.write(createSparseMatrix, tmp);
		//		Ismll2Arff a = new Ismll2Arff();
		//		a.setInput(tmp);
		//		a.setOutput(out);
		//
		//		a.setProgressCounter(10000);
		//		a.run();
		//		System.out.println("ISMLL-Size: " + tmp.length());
		//		System.out.println("ARFF-Size: " + out.length());
		Matrix read = Matrices.readWeka0(out);

		//		checkSame(createSparseMatrix, read);
	}

	//	@Test
	public void testBackAndForthAllNumericals() {
		DefaultMatrix in = super.createSparseMatrix(10, 100, 0);
		Instances wekaInstances = Matrices.wekaInstances(in, new ArffEncoder() {

			@Override
			public String getName() {
				return "JUnit-Test";
			}

			//			@Override
			//			public String getWekaTypeDescription(int column) {
			//				return NUMERIC;
			//			}

			@Override
			public String getAttributeName(int i) {
				return "junitattr" + i;
			}

			@Override
			public String encode(int col, float value) {
				return value + "";
			}

			@Override
			public Type getAttributeType(int column) {
				return Type.Numeric;
			}

			//			@Override
			//			public Map<Integer, String> getMap(int column) {
			//				return Collections.emptyMap();
			//			}
		});
		Matrix result = Matrices.asMatrix(wekaInstances);

		Assert.assertEquals(in.getNumColumns(), result.getNumColumns());
		Assert.assertEquals(in.getNumRows(), result.getNumRows());
		super.checkSame(in, result);

	}


	//	@Test
	public void testBackAndForthWithNominals() {
		final int[] nominalCols = new int[] {1,4,5,6};
		final int[] diffValues = new int[] {2,2,3,2};
		final String[] nominalStrings = new String[4];
		DefaultMatrix in = super.createSparseMatrix(10, 100, 0, nominalCols, diffValues);
		final TreeSet<?>[] tsstring = new TreeSet<?>[4];
		final Map<Integer, Map<Integer, String>> encodings = new HashMap<Integer, Map<Integer,String>>();
		Map<Integer, String> m1 = new HashMap<Integer, String>();
		m1.put(Integer.valueOf(0), "0.0");
		m1.put(Integer.valueOf(1), "1.0");
		encodings.put(Integer.valueOf(1), m1);
		Map<Integer, String> m4 = new HashMap<Integer, String>();
		m4.put(Integer.valueOf(0), "0.0");
		m4.put(Integer.valueOf(1), "1.0");
		encodings.put(Integer.valueOf(4), m4);
		Map<Integer, String> m5 = new HashMap<Integer, String>();
		m5.put(Integer.valueOf(0), "0.0");
		m5.put(Integer.valueOf(1), "1.0");
		m5.put(Integer.valueOf(2), "2.0");
		encodings.put(Integer.valueOf(5), m5);
		Map<Integer, String> m6 = new HashMap<Integer, String>();
		m6.put(Integer.valueOf(0), "0.0");
		m6.put(Integer.valueOf(1), "1.0");
		encodings.put(Integer.valueOf(6), m6);

		for (int i = 0; i < nominalCols.length; i++) {
			// column nominalCol[i];
			TreeSet<String> ts = new TreeSet<String>();
			for (int j = 0; j < in.getNumRows(); j++) {
				float v = in.get(j, nominalCols[i]);
				ts.add(v + "");
			}
			String input="";
			int pos =0;
			for (String s : ts) {
				if (pos>0)
					input+=",";
				input+=s;
			}
			nominalStrings[i]="{" + input + "}";
			tsstring[i]=ts;

		}

		Instances wekaInstances = Matrices.wekaInstances(in, new ArffEncoder() {

			@Override
			public String getName() {
				return "JUnit-Test2";
			}

			//			@Override
			//			public String getWekaTypeDescription(int column) {
			//				if (Arrays.binarySearch(nominalCols, column)>=0) {
			//					// nominal
			//					return nominalStrings[column];
			//				}
			//
			//				return NUMERIC;
			//			}

			@Override
			public String getAttributeName(int i) {
				return "junitattr" + i;
			}

			@Override
			public String encode(int col, float value) {

				return value + "";
			}
			@Override
			public Type getAttributeType(int column) {
				if (Arrays.binarySearch(nominalCols, column)>=0) {
					// nominal
					return Type.Nominal;
				}
				return Type.Numeric;
			}

			//			@Override
			//			public Map<Integer, String> getMap(int column) {
			//				return encodings.get(Integer.valueOf(column));
			//			}
		});
		Matrix result = Matrices.asMatrix(wekaInstances);

		Assert.assertEquals(in.getNumColumns(), result.getNumColumns());
		Assert.assertEquals(in.getNumRows(), result.getNumRows());
		super.checkSame(in, result);

	}
}
