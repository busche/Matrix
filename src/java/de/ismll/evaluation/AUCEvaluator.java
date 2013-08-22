/**
 * AUCEvaluator.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.evaluation;

import de.ismll.table.Vector;
import de.ismll.table.Vectors;

/**
 * A simple AUC classifier evaluator.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public class AUCEvaluator implements IEvaluator {

	protected float min_pred = -1;
	protected float max_pred = 1;
	protected float step = 0.05f;

	/**
	 * Evaluate the predictions of a classifier.
	 **/
	public float evaluate(Vector targetsTestTrue, Vector targetsTestPredicted) {
		int pos = Vectors.count(targetsTestTrue, 1.f, 0.0001f);
		int neg = Vectors.count(targetsTestTrue, -1.f, 0.0001f);

		float threshold = min_pred;
		float sens0 = 1.0f;
		float spec0 = 0.0f;

		float area = 0;

		while (threshold <= max_pred) {
			int true_pos=0;
			int true_neg=0;

			for (int i = 0; i < targetsTestTrue.size(); i++) {
				int targetPredicted = (targetsTestPredicted.get(i) > threshold ? 1 : -1);
				if (targetPredicted == targetsTestTrue.get(i)) {
					if (targetPredicted == 1)
						++true_pos;
					else
						++true_neg;
				}
			}
			float sens = true_pos * 1.0f / pos;
			float spec = true_neg * 1.0f / neg;

			area += ((spec - spec0) * (sens + sens0)/2);

			threshold += step;
			sens0 = sens;
			spec0 = spec;
		}

		return area;
	}


	/**
	 * Compare two scores by this evaluator and return +1 if score1 is better than score2,
	 * -1 if score2 is better than score1 and 0 if both are equally good.
	 */
	public float compare(float score1, float score2) {
		return score1 > score2 ? +1 : score2 > score1 ? -1 : 0;
	}
}