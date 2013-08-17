package de.ismll.table.projections;

import junit.framework.Assert;

import org.junit.Test;

import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultVector;

public class ColumnUnionVectorViewTest {

	@Test
	public void testView1() {
		float[]v1= new float[] {1.2f, 1.5f, 4.3f,.44f};
		float[]v2= new float[] {2*1.2f, 3*1.5f, 4*4.3f,4*.44f};
		float[]v3= new float[] {7*1.2f, 5*1.5f, 5*4.3f,5*.44f,.665f, 1f};

		DefaultVector ve1 = new DefaultVector(v1);
		DefaultVector ve2 = new DefaultVector(v2);
		DefaultVector ve3 = new DefaultVector(v3);
		ColumnUnionVectorView v = new ColumnUnionVectorView(new Vector[] {ve1,ve2, ve3});
		Assert.assertEquals(v1.length +v2.length + v3.length, v.size());
		for (int i = 0; i < v1.length; i++)
			Assert.assertEquals(v1[i], v.get(i), 0.001);
		for (int i = 0; i < v2.length; i++)
			Assert.assertEquals(v2[i], v.get(v1.length + i), 0.001);
		for (int i = 0; i < v3.length; i++)
			Assert.assertEquals(v3[i], v.get(v1.length + v2.length + i), 0.001);

	}
}
