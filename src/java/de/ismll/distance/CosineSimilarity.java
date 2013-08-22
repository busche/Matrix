package de.ismll.distance;

import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultVector;

public class CosineSimilarity implements IDistanceMeasure{

	@Override
	public double distance(Vector a, Vector b) {
		double dot = Vectors.dotProduct(a,b);

		return (dot/(Vectors.norm(a) * Vectors.norm(b)));
	}


	public static void main(String[] args) {
		// Tests:

		DefaultVector a = new DefaultVector(new float[] {
				1,
				2,
				3,
				4,
				5,
				6
		});

		CosineSimilarity cs = new CosineSimilarity();

		System.out.println(cs.distance(a, a) + " == 1");

		DefaultVector amirror = new DefaultVector(new float[] {
				6,
				5,
				4,
				3,
				2,
				1
		});

		Vector aneg = new DefaultVector(new float[] {
				-1,
				-2,
				-3,
				-4,
				-5,
				-6
		});

		System.out.println(cs.distance(a, amirror) + " == -1");
		System.out.println(cs.distance(a, aneg) + " == -1");

	}

}
