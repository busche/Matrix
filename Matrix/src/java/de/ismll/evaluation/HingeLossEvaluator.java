/**
 * HingeLossEvaluator.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.evaluation;

import de.ismll.table.Vector;

/**
 * A simple Hinge loss classifier evaluator.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public class HingeLossEvaluator implements Evaluator {

	/**
	 * Evaluate the predictions of a classifier.
	 **/
	public float evaluate(Vector targetsTestTrue, Vector targetsTestPredicted) {
		int numInstances = targetsTestTrue.size();
		float loss = 0;
		for (int i = 0; i < numInstances; ++i)
			loss += Math.max(0, 1 - targetsTestPredicted.get(i) * targetsTestTrue.get(i));
		return loss;
	}

	/**
	 * Compare two scores by this evaluator and return +1 if score1 is better than score2,
	 * -1 if score2 is better than score1 and 0 if both are equally good.
	 */
	public float compare(float score1, float score2) {
		return score1 < score2 ? +1 : score2 < score1 ? -1 : 0;
	}

}