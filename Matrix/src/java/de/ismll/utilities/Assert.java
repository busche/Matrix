package de.ismll.utilities;


/**
 * 
 * @author Andre Busche
 */
public class Assert {

	public static void notNull(Object reference, String variable) throws NullPointerException{
		if (reference == null)
			throw new NullPointerException(variable + " should not have been null!");

	}

	public static void assertTrue(boolean b, String condition) {
		if (!b)
			throw new RuntimeException("Assertion failed. " + condition + " evaluated to false!");
	}
}
