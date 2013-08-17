package de.ismll.sampling;

import org.junit.Test;

public class MonteCarloTest {

	@Test
	public void testEnumerate() {

		MonteCarlo<String> mc = new MonteCarlo<String>();

		mc.appendProbability("b", 10);
		mc.appendProbability("a", 10);

		for (String s : mc) {
			System.out.println(s);
		}
	}
}
