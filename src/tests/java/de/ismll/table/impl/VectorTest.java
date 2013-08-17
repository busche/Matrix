package de.ismll.table.impl;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

public class VectorTest {

	//	@Test
	public void test1() throws IOException {
		File f1 = new File("H:\\KDD2010\\export_lars\\split\\extern_algebra_3.split");
		File f2 = new File("H:\\KDD2010\\export_lars\\split\\extern_algebra_3.split");
		System.out.println("Read fast");
		DefaultIntVector v2 = DefaultIntVector.read(f2);
		System.out.println("Read slow");
		DefaultIntVector v1 = DefaultIntVector.readSlow(f1);

		System.out.println("Compare ...");
		for (int i = 0; i < v1.size(); i++)
			Assert.assertEquals(v1.get(i), v2.get(i));

	}
}
