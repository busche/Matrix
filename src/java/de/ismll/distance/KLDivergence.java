package de.ismll.distance;

import de.ismll.table.Vector;

public class KLDivergence implements DistanceMeasure, SequentialDistanceMeasure {

	@Override
	public double distance(Vector h, Vector k) {
		return _Distances.measure(this, h, k);
	}

	public double distance2(Vector h, Vector k) {
		int hSize = h.size();
		if (hSize!=k.size())
			throw new RuntimeException("Vectors are of different length!");

		double sum=0.;
		for (int i = 0; i < hSize; i++) {
			float hValue = h.get(i);
			double lg = Math.log10(hValue) / Math.log10(k.get(i));
			sum += hValue * lg;
		}
		return sum;
	}

	@Override
	public double accummulate(double previousTmpValue, float x1_i, float x2_i) {
		double lg = Math.log10(x1_i) / Math.log10(x2_i);
		return previousTmpValue+ x1_i * lg;

	}

	@Override
	public double condense(double accumulatedTmpValue) {
		return accumulatedTmpValue;
	}

}
