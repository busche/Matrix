package de.ismll.evaluation;

import de.ismll.table.IntVector;
import de.ismll.table.Vector;
import de.ismll.utilities.Assert;

public class Accuracy implements IEvaluator{

	@Override
	public float evaluate(Vector targetsTestTrue, Vector targetsTestPredicted) {
		int a = targetsTestTrue.size();
		int b = targetsTestPredicted.size();
		Assert.assertTrue(a == b, "vectors have equal length (" + a + "!=" + b + ")");

		int same = 0;

		for (int i = b - 1; i >= 0; i--) {
			if (Math.abs(targetsTestPredicted.get(i)-targetsTestTrue.get(i))<0.00001) {
				same ++;
			}
		}
		return ((float)same)/b;
	}

	@Override
	public float compare(float score1, float score2) {
		return score1 > score2 ? +1 : score2 > score1 ? -1 : 0;
	}

	public float evaluate(IntVector l1, IntVector l2) {
		int a = l1.size();
		int b = l2.size();
		Assert.assertTrue(a == b, "vectors have equal length (" + a + "!=" + b + ")");

		int same = 0;

		for (int i = a - 1; i >= 0; i--) {
			if (l1.get(i) == l2.get(i)) {
				same ++;
			}
		}
		return ((float)same)/a;
	}

}
