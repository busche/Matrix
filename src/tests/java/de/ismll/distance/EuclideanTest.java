package de.ismll.distance;

import junit.framework.Assert;

import org.junit.Test;

import de.ismll.table.impl.DefaultVector;

public class EuclideanTest {


	public double euclid(float[] a, float[]b) {
		double sum = 0;
		for (int i = 0; i < a.length; i++) {
			sum += (a[i]-b[i])*(a[i]-b[i]);
		}
		return Math.sqrt(sum);
	}

	@Test
	public void testEuclidean() {
		float[] a = new float[10];
		float[] b = new float[10];
		fillrandom(a);
		fillrandom(b);

		double expected = euclid(a, b);
		DefaultVector dv1 = new DefaultVector(a);
		DefaultVector dv2 = new DefaultVector(b);
		EuclideanDistanceMeasure e = new EuclideanDistanceMeasure();
		double is = e.distance(dv1, dv2);
		Assert.assertEquals(expected, is, 0.001d);

	}

	private void fillrandom(float[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = (float) Math.random();
		}
	}
}
