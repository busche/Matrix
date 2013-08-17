package de.ismll.classifier.wekabridge;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import de.ismll.bootstrap.BootstrapException;
import de.ismll.bootstrap.CommandLineParser;
import de.ismll.bootstrap.Parameter;
import de.ismll.classifier.NominalClassifier;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.projections.ColumnUnionMatrixView;
import de.ismll.utilities.Buffer;
import de.ismll.utilities.Tools;

/**
 * Bootstrap-syntax: c:/work/wekabattery-1-40-256-0/model,0,1
 * 
 * 
 * @author Andre Busche
 *
 */
public class DeserializedWekaClassifier implements NominalClassifier{

	private Logger logger = LogManager.getLogger(getClass());

	private Classifier classifier;


	protected TreeSet<String> manualClassmap;

	Set<String> initializeClassmap() {
		if (manualClassmap == null) {
			manualClassmap = new TreeSet<String>();
		}
		return manualClassmap;
	}

	public DeserializedWekaClassifier(Classifier c) {
		classifier = c;
	}

	public static DeserializedWekaClassifier convert(Object in) {
		File modelFile = null;
		String sourceStr = null;
		if (in instanceof File) {
			modelFile = (File)in;
		} else if (in instanceof String) {
			sourceStr= (String) in;
		} else {
			sourceStr = in.toString();
		}
		DeserializedWekaClassifier ret = null;
		if (sourceStr !=null) { // && modelFile == null

			String[] parts = Tools.split(sourceStr, CommandLineParser.ARRAY_DELIMITER_CHAR);
			if (parts != null && parts.length>0) {
				File f = new File(parts[0]);
				if (f.isFile()) {
					ret = convert(f);
				}
			}
			if (ret == null) {
				throw new BootstrapException("Could not split " + sourceStr + " using (" + CommandLineParser.ARRAY_DELIMITER + ") into at least one piece. Need at least the classifier name.");
			}
			Set<String> labels = ret.initializeClassmap();
			for (int i = 1; i < parts.length; i++) {
				labels.add(parts[i]);
			}
		}


		if (ret == null && modelFile!=null) {
			Classifier read;
			try {
				read = (Classifier) weka.core.SerializationHelper.read(Buffer.newInputStream(modelFile));
			} catch (Exception e) {
				throw new BootstrapException(e);
			}
			ret = new DeserializedWekaClassifier(read);
		}

		return ret;
	}

	@Override
	public Vector getHyperparameters() {
		return hyperparameters;
	}

	Vector hyperparameters;
	@Override
	public void setHyperparameters(Vector hyperparameters) {
		this.hyperparameters = hyperparameters;

	}

	@Override
	public NominalClassifier copy() throws Exception {
		Classifier copy = AbstractClassifier.makeCopy(classifier);
		return new DeserializedWekaClassifier(copy);
	}

	@Override
	public void train(Matrix predictorsTrain, IntVector targetsTrain)
			throws Exception {
		throw new Exception("Deserialized model. Cannot retrain.");
	}


	private MyArffEncoder enc;

	private MyTestdataArffEncoder myTestdataArffEncoder;

	@Parameter(cmdline="progressreporter")
	private int progressreporter=-1;


	@Override
	public IntVector predict(Matrix predictorsTest) throws Exception {

		if (myTestdataArffEncoder == null) {
			logger.warn("No test dataset arff encoder initialized. Maybe the model was deserialized? I am trying my best to keep things running...");
			enc = new MyArffEncoder(predictorsTest.getNumColumns()+1);
			myTestdataArffEncoder = new MyTestdataArffEncoder(enc);
			int idx=0;
			for (String s : manualClassmap) {
				myTestdataArffEncoder.map.put(Integer.valueOf(idx++), s);
			}

			//			myTestdataArffEncoder.map=ArffEncoderHelper.getWekaNominalMap(enc, predictorsTest.getNumColumns()-1, predictorsTest);

		}

		DefaultIntVector predictions = new DefaultIntVector(predictorsTest.getNumRows());
		logger.debug("Predicting class labels for " + predictorsTest.getNumRows() + " instances.");

		DefaultMatrix unknown = new DefaultMatrix(predictorsTest.getNumRows(), 1);
		Matrices.set(unknown, -1);

		logger.debug("Converting to WEKA-Instances");
		// todo: this is wasting memory, as the matrix is always copied
		Matrix data=new DefaultMatrix(new ColumnUnionMatrixView(new Matrix[] { predictorsTest, unknown }));
		Instances w = Matrices.wekaInstances(data, myTestdataArffEncoder);
		w.setClassIndex(w.numAttributes()-1);

		logger.debug("Classifying individual instances...");
		try {

			for (int i = 0; i < w.numInstances(); i++) {
				if (progressreporter > 0 && i % progressreporter == 0) {
					logger.info("Completed " + progressreporter + " instances.");
				}
				Instance currentInstance = w.instance(i);
				float prediction = (float) classifier.classifyInstance(currentInstance);
				String value = currentInstance.classAttribute().value((int) prediction);
				int convertedClassLabel = Double.valueOf(value).intValue();
				predictions.set(i,  convertedClassLabel);

				//				predictions.set(i, (int) prediction);
			}
		} catch (Exception e) {
			throw e;
		}

		return predictions;
	}



	public int predict(Vector in) throws Exception {
		Matrix d = new DefaultMatrix(1, in.size());
		Matrices.setRow(d, 0, in);
		IntVector predict = predict(d);
		return predict.get(0);
	}

	public int getProgressreporter() {
		return progressreporter;
	}

	public void setProgressreporter(int progressreporter) {
		this.progressreporter = progressreporter;
	}

}
