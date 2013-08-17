package de.ismll.table;

import java.util.Arrays;

import org.junit.Assert;

public class VectorsTest {

	//	@Test
	public void testStringEncoding(){
		String[] in = new String[] {
				"test",
				"-K 10",
				"-L 12"
		};

		Vector encodeUglyStringArray = Vectors.encodeUglyStringArray(in);
		//		System.out.println(Vectors.toString(encodeUglyStringArray));
		String[] decodeUglyStringVector = Vectors.decodeUglyStringVector(encodeUglyStringArray);

		System.out.println(Arrays.deepToString(decodeUglyStringVector));
		Assert.assertArrayEquals(in, decodeUglyStringVector);

	}

}
