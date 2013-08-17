package de.ismll.table.impl;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.MatrixTest;


public class SparseMatrixTest extends MatrixTest{

	//	@Test
	public void writeAndReadSparse() throws IOException {
		int cols = 100;
		int rows = 20;
		float sparsity=0.4f;

		DefaultMatrix original = createSparseMatrix(cols, rows, sparsity);

		File createTempFile = File.createTempFile("tmp", "fle");
		createTempFile.deleteOnExit();

		Matrices.writeSparse(original, createTempFile.getAbsolutePath());
		Matrices.writeSparse(original, createTempFile.getAbsolutePath());

		SparseRowMatrix deserialized = SparseRowMatrix.read(createTempFile, 10);

		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				Assert.assertEquals(original.get(i, j), deserialized.get(i, j), 0.0001);


	}


	//	@Test
	public void writeAndReadSparseLibSVM() throws IOException {
		int cols = 20;
		// 20million
		//		int rows = 5000;
		int rows = 20000000;
		float sparsity=0.7f;
		//		float[] test=  new float[10000];
		DefaultMatrix original = new DefaultMatrix(rows, cols);
		DefaultVector labels = new DefaultVector(rows);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				float val = (float) Math.random();
				original.set(i, j, (val<sparsity?0.0f:val));
			}
			labels.set(i, (Math.random()>0.5?1:-1));

		}
		File createTempFile = File.createTempFile("tmp", "fle");
		//		createTempFile.deleteOnExit();


		//		Matrices.writeSparseLibSVM(original, labels, createTempFile);
		//		Matrices.writeSparse(original, File.createTempFile("tmp", "sparse").getAbsolutePath());
		Matrices.write(original, File.createTempFile("tmp", "full"));
		//		Matrices.writeSparse(original, createTempFile.getAbsolutePath());

		//		Matrix deserialized = SparseRowMatrix.readLibSVM(createTempFile, 1000);
		//
		//		for (int i = 0; i < rows; i++)
		//			for (int j = 0; j < cols; j++) {
		//				float a = original.get(i, j);
		//				float b = deserialized.get(i, j+1);
		//				Assert.assertEquals(a,b , 0.0001);
		//			}

		System.out.print("input?");
		System.in.read();
	}

	//	@Test
	public void testRead() throws IOException {
		String fle="tmp5451229103855635585fle";
		File f = new File("C:/Users/busche/AppData/Local/Temp/" + fle);
		Matrix deserialized = SparseRowMatrix.readLibSVM(f, 10000);
		//		Matrix deserialized = DefaultMatrix.read(f.getAbsolutePath(), 10000);

		System.out.print("input?");
		System.in.read();
	}

	//	@Test
	public void dumpSparseMatrix() throws IOException {
		DefaultMatrix createSparseMatrix = createSparseMatrix(2, 5, 0);
		File createTempFile = File.createTempFile("sparse", "test");
		Matrices.writeSparse(createSparseMatrix, createTempFile.getAbsolutePath());
		System.out.println(createTempFile);
		System.in.read();

	}

}
