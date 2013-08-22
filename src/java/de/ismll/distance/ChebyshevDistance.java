package de.ismll.distance;

import de.ismll.table.Vector;

public class ChebyshevDistance implements IDistanceMeasure,  SequentialDistanceMeasure {

	@Override
	public double distance(Vector h, Vector k) {
		return _Distances.measure(this, h, k);
	}

	public double accummulate(double previousTmpValue, float x1_i, float x2_i) {

		double diff = Math.abs(x1_i-x2_i);

		return Math.max(previousTmpValue, diff);
	}

	public double condense(double accumulatedTmpValue) {
		return accumulatedTmpValue;
	}

}
