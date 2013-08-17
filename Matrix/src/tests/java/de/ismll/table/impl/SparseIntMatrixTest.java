package de.ismll.table.impl;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;


public class SparseIntMatrixTest {

	//	@Test
	public void writeAndReadSparse() throws IOException {
		int cols = 100;
		int rows = 20;
		float sparsity=0.4f;

		DefaultIntMatrix original = createSparseMatrix(cols, rows, sparsity);

		File createTempFile = File.createTempFile("tmp", "fle");
		createTempFile.deleteOnExit();

		Matrices.writeSparse(original, createTempFile);
		Matrices.writeSparse(original, createTempFile);

		SparseRowIntMatrix deserialized = SparseRowIntMatrix.read(createTempFile, 100);
		System.out.println(deserialized.toString());
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				Assert.assertEquals(original.get(i, j), deserialized.get(i, j));


	}

	static DefaultIntMatrix createSparseMatrix(int cols, int rows, float sparsity) {
		DefaultIntMatrix original = new DefaultIntMatrix(rows, cols);
		int mult = 10000;
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++) {
				int val = (int) (Math.random()*mult);
				original.set(i, j, (int) (val<(sparsity*mult)?0.0f:val));
			}
		return original;
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

}
