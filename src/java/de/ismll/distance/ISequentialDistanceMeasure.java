package de.ismll.distance;

/**
 * 
 * A sequential Distance measure which is based on distances between individual dimensions.
 * 
 * @author Andre
 *
 */
public interface ISequentialDistanceMeasure {

	/**
	 * during distance calculation, accumultes an old (partial) distance value (previousTmpValue) with the new one calculated between the two given values x1_i and x2_i for the dimension i.
	 * 
	 * example: in d(x_1, x_2)=\sqrt{\sum_{i=0}^{k} (x_{1,i}-x_{2,i})^2 } this method would calculate the <code>(x_{1,i}-x_{2,i})^2</code> part along with adding this value to the temporary value implicitly required for the \sum operator (these partial values are stored in previousTmpValue).
	 * 
	 * 
	 * 
	 * @param previousTmpValue
	 * @param x1_i
	 * @param x2_i
	 * @return
	 */
	public double accummulate(double previousTmpValue, float x1_i, float x2_i);

	/**
	 * condenses the previousTmpValue tp the "final" distance between two instances.
	 * 
	 * example: in d(x_1, x_2)=\sqrt{\sum_{i=0}^{k} (x_{1,i}-x_{2,i})^2 } this method would compute the \sqrt for the accumulatedTmpValue, the individual sum of differences per dimension between the objects.
	 * 
	 * @param accumulatedTmpValue
	 * @return
	 */
	public double condense(double accumulatedTmpValue);

}
