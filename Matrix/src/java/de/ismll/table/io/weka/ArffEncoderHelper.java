package de.ismll.table.io.weka;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.ismll.table.IntVector;
import de.ismll.table.Matrix;
import de.ismll.table.io.weka.ArffEncoder.Type;

public class ArffEncoderHelper {

	public static String createNominalIdentifier(Map<Integer, String> map) {
		return createNominalIdentifier(map.values());
	}

	public static String createNominalIdentifier(Collection<String> values) {

		StringBuffer sb = new StringBuffer();
		boolean appendComma = false;
		sb.append("{");
		for (String s2 : values) {
			if (appendComma)
				sb.append(",");
			sb.append(s2);
			appendComma = true;
		}
		sb.append("}");
		return sb.toString();

	}


	public static String getWekaTypeDescription(ArffEncoder arff, int column) {
		Map<Integer, String> map=Collections.emptyMap();
		return getWekaTypeDescription(arff, column, map);
	}

	public static String getWekaTypeDescription(ArffEncoder arff, int column, Map<Integer, String> map) {
		if (arff.getAttributeType(column).equals(Type.Numeric))
			return ArffEncoder.NUMERIC;
		if (arff.getAttributeType(column).equals(Type.Nominal)) {
			return createNominalIdentifier(map);
		}
		return null;
	}


	/**
	 * tries to infer the nominal type description by usilizing the encode()-method in the encoder.
	 * 
	 * @param enc
	 * @param column
	 * @param inferFrom
	 * @return
	 */
	public static String getWekaTypeDescription(ArffEncoder enc, int column, Matrix inferFrom) {
		Map<Integer, String> wekaNominalMap;
		if (enc.getAttributeType(column).equals(Type.Nominal)) {
			wekaNominalMap = getWekaNominalMap(enc, column, inferFrom);
		} else
			wekaNominalMap = Collections.emptyMap();

		return getWekaTypeDescription(enc, column, wekaNominalMap);
	}

	/**
	 * tries to infer the nominal type description by usilizing the encode()-method in the encoder.
	 * 
	 * @param enc
	 * @param column
	 * @param inferFrom
	 * @return
	 */
	public static Map<Integer, String> getWekaNominalMap(ArffEncoder enc, int column, Matrix inferFrom) {
		Map<Integer, String> map = new TreeMap<Integer, String>();
		for (int i = 0; i < inferFrom.getNumRows(); i++) {
			float f = inferFrom.get(i, column);
			Integer key = Integer.valueOf((int)f);
			String value = enc.encode(column, f);
			if (!map.containsKey(key))
				map.put(key, value);
		}

		return map;
	}

	public static String getWekaTypeDescription(IntVector inferFrom) {
		Set<String> set = new TreeSet<String>();
		for (int i = 0; i < inferFrom.size(); i++) {
			int f = inferFrom.get(i);
			Integer key = Integer.valueOf(f);

			set.add(key + "");
		}

		return createNominalIdentifier(set);
	}
}
