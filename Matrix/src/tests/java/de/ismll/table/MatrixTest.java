package de.ismll.table;

import java.util.Arrays;

import org.junit.Assert;

import de.ismll.table.impl.DefaultMatrix;

public class MatrixTest {

	protected DefaultMatrix createSparseMatrix(int cols, int rows, float sparsity) {
		DefaultMatrix original = new DefaultMatrix(rows, cols);
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++) {
				float val = (float) Math.random();
				original.set(i, j, (val<sparsity?0.0f:val));
			}
		return original;
	}

	protected DefaultMatrix createSparseMatrix(int cols, int rows, float sparsity, int[] categorialColumns, int[] differentValuesPerCategorialIndex) {
		DefaultMatrix original = new DefaultMatrix(rows, cols);

		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++) {
				float val;
				int idx;
				if ((idx=Arrays.binarySearch(categorialColumns, j))>=0)
					val = (int)(Math.random()*differentValuesPerCategorialIndex[idx]);
				else
					val = (float) Math.random();
				original.set(i, j, (val<sparsity?0.0f:val));
			}
		return original;
	}

	protected void checkSame(Matrix am, Matrix bm) {
		Assert.assertEquals(am.getNumColumns(), bm.getNumColumns());
		Assert.assertEquals(am.getNumRows(), bm.getNumRows());
		for (int i = 0; i < am.getNumRows(); i++)
			for (int j = 0; j < bm.getNumColumns(); j++) {
				float a = am.get(i, j);
				float b = bm.get(i, j);
				Assert.assertEquals(a,b , 0.0001);
			}
	}

	protected void checkSame(IntMatrix am, IntMatrix bm) {
		Assert.assertEquals(am.getNumColumns(), bm.getNumColumns());
		Assert.assertEquals(am.getNumRows(), bm.getNumRows());
		for (int i = 0; i < am.getNumRows(); i++)
			for (int j = 0; j < bm.getNumColumns(); j++) {
				int a = am.get(i, j);
				int b = bm.get(i, j);
				Assert.assertEquals(a,b);
			}
	}

	protected boolean returnSame(Matrix am, Matrix bm) {
		boolean ret = true;
		ret |= am.getNumColumns() == bm.getNumColumns();
		ret |= am.getNumRows()== bm.getNumRows();
		if (!ret) return false;

		for (int i = 0; i < am.getNumRows(); i++)
			for (int j = 0; j < bm.getNumColumns(); j++) {
				float a = am.get(i, j);
				float b = bm.get(i, j);
				ret |= Math.abs(a-b)>0.0001;
				if (!ret) return false;
			}
		return true;
	}
}
