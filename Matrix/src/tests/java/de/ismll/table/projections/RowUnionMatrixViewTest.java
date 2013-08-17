package de.ismll.table.projections;

import org.junit.Assert;
import org.junit.Test;

import de.ismll.table.Matrix;
import de.ismll.table.MatrixTest;
import de.ismll.table.impl.DefaultIntVector;

public class RowUnionMatrixViewTest extends MatrixTest{

	@Test
	public void testView1() {
		Matrix m1 = super.createSparseMatrix(10, 14, 0.1f);
		Matrix m2 = super.createSparseMatrix(10, 18, 0.4f);
		Matrix m3 = super.createSparseMatrix(10, 10, 0.0f);

		RowUnionMatrixView view= new RowUnionMatrixView(new Matrix[] {m1, m2, m3});
		Assert.assertEquals(m1.getNumRows()+m2.getNumRows()+m3.getNumRows(), view.getNumRows());
		Assert.assertEquals(m1.getNumColumns(), view.getNumColumns());

		int[] indizesM1 = new int[m1.getNumRows()];
		for (int i = 0; i < indizesM1.length; i++) {
			indizesM1[i] = i;
		}
		RowSubsetMatrixView v2 = new RowSubsetMatrixView(view, new DefaultIntVector(indizesM1));
		super.checkSame(m1, v2);

		int[] indizesM2 = new int[m2.getNumRows()];
		for (int i = 0; i < indizesM2.length; i++) {
			indizesM2[i] = i + m1.getNumRows();
		}
		RowSubsetMatrixView v3 = new RowSubsetMatrixView(view, new DefaultIntVector(indizesM2));
		super.checkSame(m2, v3);

		int[] indizesM3 = new int[m3.getNumRows()];
		for (int i = 0; i < indizesM3.length; i++) {
			indizesM3[i] = i + m1.getNumRows() + m2.getNumRows();
		}
		RowSubsetMatrixView v4 = new RowSubsetMatrixView(view, new DefaultIntVector(indizesM3));
		super.checkSame(m3, v4);


	}
}
