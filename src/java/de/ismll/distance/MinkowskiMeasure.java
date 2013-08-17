package de.ismll.distance;

import de.ismll.table.Vector;

public class MinkowskiMeasure implements DistanceMeasure, SequentialDistanceMeasure {

	@Override
	public double distance(Vector h, Vector k) {
		return _Distances.measure(this, h, k);
	}

	public MinkowskiMeasure() {
		super();
		p=1;
	}

	public MinkowskiMeasure(int p) {
		super();
		this.p = p;
	}

	private int p;

	public double accummulate(double previousTmpValue, float x1_i, float x2_i) {

		double g =  Math.abs(x1_i-x2_i);
		double ret = g;
		int cnt = 1;
		while (cnt < p) {
			cnt++;
			ret *= g;
		}
		return previousTmpValue + ret;
	}

	public double condense(double accumulatedTmpValue) {
		if (p == 1)
			return accumulatedTmpValue;
		if (p == 2)
			return Math.sqrt(accumulatedTmpValue);
		return Math.pow(accumulatedTmpValue, ((1/(double)p)));
	}

	public void setP(int p) {
		this.p = p;
	}

	public int getP() {
		return p;
	}

}
