/**
 * AUCEvaluator.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.evaluation;

import de.ismll.table.Vector;

/**
 * A simple misclassification rate classifier evaluator for binary classification -1/+1 predicted labels.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public class MisclassificationRateEvaluator implements Evaluator {

	float threshold;

	public MisclassificationRateEvaluator(float threshold) {
		this.threshold = threshold;
	}

	/**
	 * Evaluate the predictions of a classifier.
	 **/
	public float evaluate(Vector targetsTestTrue, Vector targetsTestPredicted) {
		int numInstances = targetsTestTrue.size();
		if (numInstances == 0) {
			System.out.println("ERROR: you cannot evaluate on 0 examples.");
			System.exit(-1);
		}
		int misses = 0;
		int nans = 0;
		for (int i = 0; i < numInstances; ++i)
			if ((targetsTestPredicted.get(i) - threshold) * targetsTestTrue.get(i) < 0)
				++misses;
			else if (Float.isNaN(targetsTestPredicted.get(i)))
				++nans;
		float score = (misses + nans) * 1.0f / numInstances;
		if (nans > 0)
			System.out.println("there are " + nans + " NaNs in the prediction!");
		if (Float.isNaN(score)) {
			System.out.println("STRANGE: a score of NaN");
			System.exit(-1);
		}
		return score;
	}

	/**
	 * Compare two scores by this evaluator and return +1 if score1 is better than score2,
	 * -1 if score2 is better than score1 and 0 if both are equally good.
	 */
	public float compare(float score1, float score2) {
		return score1 < score2 ? +1 : score2 < score1 ? -1 : 0;
	}
}