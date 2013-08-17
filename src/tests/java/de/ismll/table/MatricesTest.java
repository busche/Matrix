package de.ismll.table;

import java.io.File;
import java.io.IOException;

import de.ismll.table.impl.DefaultMatrix;

public class MatricesTest extends MatrixTest{


	public static DefaultMatrix createMatrix(int numRows, int numCols) {
		DefaultMatrix ret = new DefaultMatrix(numRows, numCols);

		for (int i = 0; i < numCols; i++)
			for (int j = 0; j < numRows; j++)
				ret.set(j, i, (float) (Math.random()*10000));
		return ret;
	}


	//	@Test
	public void testBinary() throws IOException {
		DefaultMatrix createMatrix = createMatrix(100, 100);
		File createTempFile = File.createTempFile("sdfsdf", null);
		Matrices.writeBinary(createMatrix, createTempFile, Matrices.BINARY_UNCOMPRESSED);
		Matrix readBinary = Matrices.readBinary(createTempFile);
		checkSame(createMatrix, readBinary);
	}


}
