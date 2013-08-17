package de.ismll.table.projections;

import junit.framework.Assert;

import org.junit.Test;

import de.ismll.table.IntMatrix;
import de.ismll.table.impl.DefaultIntMatrix;
import de.ismll.table.impl.IntMatrixTest;


public class ColumnUnionIntMatrixViewTest {

	@Test
	public void testGet() {

		DefaultIntMatrix mat1=IntMatrixTest.createMatrix(100, 20);
		DefaultIntMatrix mat2=IntMatrixTest.createMatrix(100, 20);

		ColumnUnionIntMatrixView view = new ColumnUnionIntMatrixView(new IntMatrix[] {mat1, mat2});
		Assert.assertEquals(100, view.getNumRows());
		Assert.assertEquals(40, view.getNumColumns());

		for (int row = 0; row < view.getNumRows(); row++) {
			for (int i = 0; i < 20; i++)
				Assert.assertEquals(mat1.get(row, i), view.get(row, i));
			for (int i = 0; i < 20; i++)
				Assert.assertEquals(mat2.get(row, i), view.get(row, i+20));
		}
	}

	@Test
	public void testGet2() {
		DefaultIntMatrix mat1=IntMatrixTest.createMatrix(100, 20);
		DefaultIntMatrix mat2=IntMatrixTest.createMatrix(100, 10);
		DefaultIntMatrix mat3=IntMatrixTest.createMatrix(100, 30);

		ColumnUnionIntMatrixView view = new ColumnUnionIntMatrixView(new IntMatrix[] {mat1, mat2, mat3});
		Assert.assertEquals(100, view.getNumRows());
		Assert.assertEquals(60, view.getNumColumns());

		for (int row = 0; row < view.getNumRows(); row++) {
			for (int i = 0; i < 20; i++)
				Assert.assertEquals(mat1.get(row, i), view.get(row, i));
			for (int i = 0; i < 10; i++)
				Assert.assertEquals(mat2.get(row, i), view.get(row, i+20));
			for (int i = 0; i < 30; i++)
				Assert.assertEquals(mat3.get(row, i), view.get(row, i+30));
		}
	}

	@Test
	public void testGet3() {
		DefaultIntMatrix mat1=IntMatrixTest.createMatrix(100, 20);
		DefaultIntMatrix mat3=IntMatrixTest.createMatrix(100, 30);

		ColumnUnionIntMatrixView view = new ColumnUnionIntMatrixView(new IntMatrix[] {mat1, mat3});
		Assert.assertEquals(100, view.getNumRows());
		Assert.assertEquals(50, view.getNumColumns());

		for (int row = 0; row < view.getNumRows(); row++) {
			for (int i = 0; i < 20; i++)
				Assert.assertEquals(mat1.get(row, i), view.get(row, i));
			for (int i = 0; i < 30; i++)
				Assert.assertEquals(mat3.get(row, i), view.get(row, i+20));
		}
	}
}
