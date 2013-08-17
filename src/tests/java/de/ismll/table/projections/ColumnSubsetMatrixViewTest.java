package de.ismll.table.projections;

import org.junit.Assert;
import org.junit.Test;

import de.ismll.table.impl.DefaultMatrix;

public class ColumnSubsetMatrixViewTest {

	public static final double EPSILON = 0.000001;

	@Test
	public void testProjection() {
		DefaultMatrix matrix = DefaultMatrix.wrap(new float[][] {
				new float[] {1.0f, 2.0f, 3.0f},
				new float[] {4.0f, 5.0f, 6.0f},
				new float[] {7.0f, 8.0f, 9.0f},
				new float[] {10.0f, 11.0f, 12.0f},

		});

		ColumnSubsetMatrixView view = new ColumnSubsetMatrixView(matrix, new int[] {0,2});

		Assert.assertEquals(2, view.getNumColumns());
		Assert.assertEquals(3, matrix.getNumColumns());

		Assert.assertEquals(matrix.getNumRows(), view.getNumRows());


		Assert.assertEquals(matrix.get(0, 2), view.get(0, 1), EPSILON);
		Assert.assertEquals(matrix.get(0, 0), view.get(0, 0),EPSILON);

		Assert.assertEquals(matrix.get(1, 2), view.get(1, 1),EPSILON);
		Assert.assertEquals(matrix.get(1, 0), view.get(1, 0),EPSILON);

		Assert.assertEquals(matrix.get(2, 2), view.get(2, 1),EPSILON);
		Assert.assertEquals(matrix.get(2, 0), view.get(2, 0),EPSILON);

		Assert.assertEquals(matrix.get(3, 2), view.get(3, 1),EPSILON);
		Assert.assertEquals(matrix.get(3, 0), view.get(3, 0),EPSILON);


	}
}
