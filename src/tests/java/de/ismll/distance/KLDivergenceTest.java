package de.ismll.distance;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultVector;

public class KLDivergenceTest {

	Vector a, b;

	@Before
	public void setup() {
		int size = 10;
		a = new DefaultVector(size);
		b = new DefaultVector(size);
		for (int i = 0; i < size; i++) {
			a.set(i, (float) Math.random());
			b.set(i, (float) Math.random());
		}
	}

	@Test
	public void test1() {
		KLDivergence d = new KLDivergence();
		double distance1 = d.distance(a, b);
		double distance2 = d.distance2(a, b);
		Assert.assertEquals(distance1, distance2, 0.0001);
	}
}
