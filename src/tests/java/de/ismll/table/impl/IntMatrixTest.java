package de.ismll.table.impl;

import java.io.File;
import java.io.IOException;

import de.ismll.table.IntMatrix;
import de.ismll.table.Matrices;
import de.ismll.table.MatrixTest;


public class IntMatrixTest extends MatrixTest {

	//	@Test
	public void testRead() throws IOException {
		File f = File.createTempFile("matrix", "test");

		DefaultIntMatrix ma = createMatrix(100,200);
		Matrices.write(ma, f);
		Matrices.debug=true;
		DefaultIntMatrix read = DefaultIntMatrix.read(f);
		Matrices.toString(read);


	}

	public static DefaultIntMatrix createMatrix(int numRows, int numCols) {
		DefaultIntMatrix ret = new DefaultIntMatrix(numRows, numCols);

		for (int i = 0; i < numCols; i++)
			for (int j = 0; j < numRows; j++)
				ret.set(j, i, (int)(Math.random()*10000));
		return ret;
	}

	//	@Test
	public void testConvertFloat2Int() throws IOException {
		DefaultMatrix createSparseMatrix = createSparseMatrix(100, 100, 30);
		File f=  File.createTempFile("matrix", "float2int");

		Matrices.write(createSparseMatrix, f);

		DefaultIntMatrix read = DefaultIntMatrix.read(f);

		System.out.println(Matrices.toString(read));
	}

	//	@Test
	public void testConvertInt2Float() throws IOException {
		DefaultIntMatrix createSparseMatrix = createMatrix(100,100);
		File f=  File.createTempFile("matrix", "float2int");

		Matrices.write(createSparseMatrix, f);

		DefaultMatrix read = DefaultMatrix.read(f);

		System.out.println(Matrices.toString(read));
	}

	//	@Test
	public void testRead2() throws IOException {
		File f = new File("H:\\export_lars\\dense_bridge_train.ismll.data");

		DefaultIntMatrix read = DefaultIntMatrix.read(f);
		System.out.println(read.getNumRows());
	}

	//	@Test
	public void testBinaryCompressed() throws IOException {
		DefaultIntMatrix createMatrix = createMatrix(100, 100);
		File createTempFile = File.createTempFile("sdfsdf", null);
		Matrices.writeIntBinary(createMatrix, createTempFile, Matrices.BINARY_COMPRESSED);
		IntMatrix readBinary = Matrices.readIntBinary(createTempFile);
		checkSame(createMatrix, readBinary);
	}
	//	@Test
	public void testBinaryUnCompressed() throws IOException {
		DefaultIntMatrix createMatrix = createMatrix(100, 100);
		File createTempFile = File.createTempFile("sdfsdf", null);
		Matrices.writeIntBinary(createMatrix, createTempFile, Matrices.BINARY_UNCOMPRESSED);
		IntMatrix readBinary = Matrices.readIntBinary(createTempFile);
		checkSame(createMatrix, readBinary);
	}
}
