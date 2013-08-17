package de.ismll.sampling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MonteCarlo<T> implements Iterable<T>{

	public  final class IteratorImpl implements Iterator<T> {

		private final Object[] v;

		private int valuesReturned=0;

		private Random random;

		private int populationSize;

		public IteratorImpl(Object[] v, long seed) {
			this.populationSize=0;
			for (Object d : v)
				populationSize+=((DT)d).relative;

			this.v = v;
			this.random = new Random(seed);
		}

		@Override
		public boolean hasNext() {
			return populationSize>0;
		}

		@Override
		public T next() {
			int next = (int) (random.nextDouble()*populationSize);
			int sum = 0;
			T ret = null;
			// get value
			for (Object d2 : v) {
				DT d =(DT)d2;
				sum += d.relative;
				if (sum > next) {
					ret=d.obj;
					d.relative--;

					assert(d.relative>=0);

					break;
				}
			}

			assert (ret!=null);
			valuesReturned++;
			populationSize--;
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private long seed;

	public MonteCarlo() {
		super();
		values=new ArrayList<DT>();
	}

	private class DT{
		T obj;
		int relative;
		@Override
		public String toString() {
			return relative + ": "+  obj.toString();
		}
	}

	private ArrayList<DT> values;

	public void appendProbability(T object, int relativeProbability) {
		DT d = new DT();
		d.obj=object;
		d.relative=relativeProbability;
		values.add(d);
	}

	public Iterator<T> getIterator(){
		Object[] v = new Object[values.size()];
		values.toArray(v);
		return new IteratorImpl(v, getSeed());
	}



	public void setSeed(long seed) {
		this.seed = seed;
	}

	public long getSeed() {
		return seed;
	}

	@Override
	public Iterator<T> iterator() {
		return getIterator();
	}

}
