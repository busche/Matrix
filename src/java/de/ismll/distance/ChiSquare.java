package de.ismll.distance;

import de.ismll.table.Vector;

public class ChiSquare implements IDistanceMeasure{

	@Override
	public double distance(Vector a, Vector b) {
		_Distances.sameDimension(a, b);
		int aSize = a.size();

		double sum=0.;
		for (int i =0; i < aSize; i++) {
			float a_i = a.get(i);
			float b_i = b.get(i);

			double m_i = (a_i+b_i)/2.;

			sum += ((a_i-m_i)*(a_i-m_i))/m_i;
		}

		return sum;
	}

}
