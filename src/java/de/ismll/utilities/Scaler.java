package de.ismll.utilities;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Scaler {

	private static Logger log = LogManager.getLogger(Scaler.class);


	/**
	 * 
	 * scales a value from source range to target range.
	 * 
	 */
	public static float scale(float source_min, float source_max, float target_min, float target_max, float value) {
		if (source_min>=source_max || target_min>=target_max) {
			postError(source_min, source_max, target_min, target_max, value);
			return value;
		}

		return ((target_max-target_min)*(value-source_min))/(source_max-source_min)+target_min;
	}

	public static double scale(double source_min, double source_max, double target_min, double target_max, double value) {
		if (target_min==target_max) {
			//			System.err.println("Constant target range input for scale(). Scaling to constant value " + target_max);
			postError(source_min, source_max, target_min, target_max, value);
			return target_max;
		}
		if (source_min>=source_max || target_min>=target_max) {
			System.err.println("Invalid input for scale. Not scaling at all.");
			postError(source_min, source_max, target_min, target_max, value);
			return value;
		}
		return ((target_max-target_min)*(value-source_min))/(source_max-source_min)+target_min;
	}


	static class DT {
		AtomicInteger counts;
		double source_min, source_max, target_min, target_max;
	}
	static HashMap<String, DT> suppressCounts = new HashMap<>();

	static boolean registered = false;

	private static void postError(double source_min, double source_max,
			double target_min, double target_max, double value) {
		double pseudokey = source_min + source_max + target_min + target_max;
		String pseudokeyS = "" + pseudokey;

		if (!suppressCounts.containsKey(pseudokeyS)) {
			log.error("Cannot scale value " + value + " from interval [" + source_min + "," + source_max + "] to [" + target_min + "," + target_max + "]. Trying to suppress multiple of these values by a crude heuristic.");
		}

		DT integer = suppressCounts.get(pseudokeyS);
		if (integer == null) {
			integer = new DT();
			integer.counts = new AtomicInteger(0);
			integer.source_min = source_min;
			integer.source_max = source_max;
			integer.target_min = target_min;
			integer.target_max = target_max;
			suppressCounts.put(pseudokeyS, integer);
		}
		integer.counts.getAndIncrement(); // could also have used integer.incrementAndGet();

		if (!registered) {
			Runtime.getRuntime().addShutdownHook(new Thread(new SuppressionRunnable(), "Conversion Statistics Printer"));
			registered = true;
			log.info("Added statistics poster at the end of the VM");
		}
	}

	private static class SuppressionRunnable implements Runnable {

		@Override
		public void run() {
			log.warn("The following invalid scaling requests were made:");
			for (Entry<String, DT> e : suppressCounts.entrySet()) {
				DT v = e.getValue();
				log.warn("Interval [" + v.source_min + "," + v.source_max + "] to [" + v.target_min + "," + v.target_max + "] : " + v.counts.get() + " times.");
			}
		}

	}
}
