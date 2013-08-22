package de.ismll.distance;

import de.ismll.table.Vector;

class _Distances {

	static void sameDimension(Vector h, Vector k) {
		int hSize = h.size();
		if (hSize!=k.size())
			throw new RuntimeException("Vectors are of different length!");

	}

	public static double measure(ISequentialDistanceMeasure dm, Vector h, Vector k) {
		sameDimension(h, k);
		int hSize = h.size();

		double sum=0.;
		for (int i = 0; i < hSize; i++) {
			float hValue = h.get(i);
			float kValue = k.get(i);
			sum = dm.accummulate(sum, hValue, kValue);
		}
		return dm.condense(sum);
	}

}
