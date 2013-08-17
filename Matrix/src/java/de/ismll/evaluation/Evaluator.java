/**
 * Evaluator.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.evaluation;

import de.ismll.table.Vector;

/**
 * A generic interface for classifier evaluators.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public interface Evaluator {

	/**
	 * Evaluate the predictions of a classifier.
	 **/
	public float evaluate(Vector targetsTestTrue, Vector targetsTestPredicted);

	/**
	 * Compare two scores by this evaluator and return +1 if score1 is better than score2,
	 * -1 if score2 is better than score1 and 0 if both are equally good.
	 */
	public float compare(float score1, float score2);

}