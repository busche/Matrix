package de.ismll.converter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Assert;
import weka.core.Instances;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.MatrixTest;
import de.ismll.table.io.weka.ArffDataset;
import de.ismll.table.io.weka.ArffEncoder;

public class Arff2IsmllTest extends MatrixTest {

	//	@Test
	public void convertArff2IsmllViaArffDataset() throws IOException {
		InputStreamReader read = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("de/ismll/converter/iris.arff"));
		Instances i = new Instances(read);

		ArffDataset m = Matrices.asArffDataset(i);

		Assert.assertEquals(i.numInstances(), m.data.getNumRows());
		Assert.assertEquals(i.numAttributes(), m.data.getNumColumns());

		Instances back = Matrices.wekaInstances(m);

		Assert.assertEquals(i.numInstances(), back.numInstances());
		Assert.assertEquals(i.numAttributes(), back.numAttributes());
		Assert.assertEquals(i.classIndex(), back.classIndex());

		for (int j = 0; j < i.numInstances(); j++)
			for (int k = 0; k < i.numAttributes(); k++) {
				double a = i.instance(j).value(k);
				double b = back.instance(j).value(k);
				System.out.println(j + " " + k + " " + a + " " + b);
				Assert.assertEquals(a,b , 0.0001);
			}

	}

	//	@Test
	public void convertArff2IsmllViaMatrix() throws IOException {
		InputStreamReader read = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("de/ismll/converter/iris.arff"));
		Instances i = new Instances(read);

		Matrix m = Matrices.asMatrix(i);

		Assert.assertEquals(i.numInstances(), m.getNumRows());
		Assert.assertEquals(i.numAttributes(), m.getNumColumns());

		Instances back = Matrices.wekaInstances(m);

		Assert.assertEquals(i.numInstances(), back.numInstances());
		Assert.assertEquals(i.numAttributes(), back.numAttributes());
		Assert.assertEquals(i.classIndex(), back.classIndex());

		for (int j = 0; j < i.numInstances(); j++)
			for (int k = 0; k < i.numAttributes(); k++) {
				double a = i.instance(j).value(k);
				double b = back.instance(j).value(k);
				Assert.assertEquals(a,b , 0.0001);
			}

	}

	//	@Test
	public void convertArff2IsmllViaMatrixAndEncoder() throws IOException {
		InputStreamReader read = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("de/ismll/converter/iris.arff"));
		final Instances i = new Instances(read);

		Matrix m = Matrices.asMatrix(i);

		Assert.assertEquals(i.numInstances(), m.getNumRows());
		Assert.assertEquals(i.numAttributes(), m.getNumColumns());

		final Map<Integer, String> map = new TreeMap<Integer, String>();
		map.put(Integer.valueOf(0), "Iris-setosa");
		map.put(Integer.valueOf(1), "Iris-versicolor");
		map.put(Integer.valueOf(2), "Iris-virginica");
		Instances back = Matrices.wekaInstances(m, new ArffEncoder() {

			@Override
			public String getName() {
				return "junit-testname";
			}
			//
			//			@Override
			//			public String getWekaTypeDescription(int column) {
			//				if (column < i.numAttributes()-1)
			//					return NUMERIC;
			//
			//				return "{Iris-setosa,Iris-versicolor,Iris-virginica}";
			//			}


			@Override
			public Type getAttributeType(int column) {
				if (column < i.numAttributes()-1)
					return Type.Numeric;
				return Type.Nominal;
			}
			//
			//			@Override
			//			public Map<Integer, String> getMap(int column) {
			//				return map;
			//			}
			//
			@Override
			public String getAttributeName(int column) {
				if (column < i.numAttributes()-1)
					return "attr" + column;
				return "clazz";
			}

			@Override
			public String encode(int column, float value) {
				if (column < i.numAttributes()-1)
					return value + "";

				return map.get(Integer.valueOf(Float.valueOf(value).intValue()));
			}
		});

		Assert.assertEquals(i.numInstances(), back.numInstances());
		Assert.assertEquals(i.numAttributes(), back.numAttributes());
		Assert.assertEquals(i.classIndex(), back.classIndex());

		for (int j = 0; j < i.numInstances(); j++)
			for (int k = 0; k < i.numAttributes(); k++) {
				double a = i.instance(j).value(k);
				double b = back.instance(j).value(k);
				Assert.assertEquals(a,b , 0.0001);
			}

	}

}
