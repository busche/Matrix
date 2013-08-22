package de.ismll.distance;

import de.ismll.table.Vector;

public class JeffreyDivergence implements IDistanceMeasure {

	@Override
	public double distance(Vector a, Vector b) {
		_Distances.sameDimension(a, b);
		int aSize = a.size();

		double sum=0.;
		for (int i =0; i < aSize; i++) {
			float a_i = a.get(i);
			float b_i = b.get(i);

			double m_i = (a_i+b_i)/2.;

			double lg_h = Math.log10(a_i/m_i);
			double lg_k = Math.log10(b_i/m_i);

			sum += a_i * lg_h + b_i * lg_k;
		}

		return sum;
	}

}
