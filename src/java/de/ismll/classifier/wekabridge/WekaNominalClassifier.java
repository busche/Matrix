package de.ismll.classifier.wekabridge;

import java.io.File;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.utilities.Tools;

/**
 * Bootstrap-conversion as follows: "classname[,options]*"
 * The options correspond to Wekas {@link AbstractClassifier#forName(String, String[])} method, while each entry in the String array corresponds to one option given from the command line.
 * 
 * Example:
 * classifier="weka.classifiers.trees.RandomForest,-K,10"
 * 
 * @author Andre Busche
 *
 */
public class WekaNominalClassifier extends AbstractWekaNominalClassifier{

	public WekaNominalClassifier copy() {
		WekaNominalClassifier ret = new WekaNominalClassifier();
		ret.setHyperparameters(getHyperparameters());
		try {
			ret.setWekaClassifier(AbstractClassifier.makeCopy(wekaClassifier));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return ret;
	}


	public static WekaNominalClassifier convert(Object src) throws Exception {
		if (src instanceof File) {
			// load model
			Classifier cls = (Classifier) weka.core.SerializationHelper.read(((File)src).getAbsolutePath());
			WekaNominalClassifier ret = new WekaNominalClassifier();
			ret.setWekaClassifier(cls);
			return ret;
		}
		Logger logger = LogManager.getLogger(AbstractWekaNominalClassifier.class);
		String use;
		if (!(src instanceof String)) {
			logger.warn("Object passed to convert()-method is no string. Try to parse its values through toString()-method.");
			use = src.toString();
		} else
			use = (String) src;

		String[] parts = Tools.split(use, CONVERT_SPLIT_CHAR);
		if (parts == null || parts.length<1) {
			throw new RuntimeException("Could not split " + use + " using (" + CONVERT_SPLIT_CHAR + ") into at least one piece. Need at lease the classifier name.");
		}

		File f = new File(parts[0]);
		if (f.isFile()) {
			WekaNominalClassifier ret =  convert(f);
			Set<String> labels = ret.initializeClassmap();
			for (int i = 1; i < parts.length; i++) {
				labels.add(parts[i]);
			}
			return ret;
		}


		String classname = parts[0];
		String[] copy = new String[parts.length-1];

		System.arraycopy(parts, 1, copy, 0, copy.length);

		Classifier forName = AbstractClassifier.forName(classname, copy);

		WekaNominalClassifier ret = new WekaNominalClassifier();
		ret.setWekaClassifier(forName);
		return ret;
	}



	@Override
	public Vector getHyperparameters() {
		if (!(super.wekaClassifier instanceof AbstractClassifier)) return super.getHyperparameters();

		AbstractClassifier ac = (AbstractClassifier) wekaClassifier;

		return Vectors.encodeUglyStringArray(ac.getOptions());
	}




	@Override
	protected void buildClassifier(Instances wtrain) throws Exception {
		super.wekaClassifier.buildClassifier(wtrain);
	}


	public int predict(Vector in) throws Exception {
		Matrix d = new DefaultMatrix(1, in.size());
		Matrices.setRow(d, 0, in);
		IntVector predict = super.predict(d);
		return predict.get(0);
	}
}
