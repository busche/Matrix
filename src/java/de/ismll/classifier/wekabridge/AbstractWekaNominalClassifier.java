package de.ismll.classifier.wekabridge;

import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import de.ismll.classifier.NominalClassifier;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.io.weka.ArffEncoderHelper;
import de.ismll.table.projections.ColumnUnionMatrixView;
import de.ismll.table.projections.IntMatrixView;
import de.ismll.table.projections.MatrixView;
import de.ismll.utilities.Assert;

public abstract class AbstractWekaNominalClassifier implements NominalClassifier {
	protected static final char CONVERT_SPLIT_CHAR = ',';

	private Logger logger = LogManager.getLogger(getClass());

	private Vector hyperparameters;

	@Override
	public Vector getHyperparameters() {
		return hyperparameters;
	}

	protected Classifier  wekaClassifier;

	private MyArffEncoder enc;

	private MyTestdataArffEncoder myTestdataArffEncoder;


	@Override
	public void setHyperparameters(Vector hyperparameters) {
		this.hyperparameters = hyperparameters;
	}

	@Override
	public void train(final Matrix predictorsTrain, IntVector targetsTrain) throws Exception {
		Assert.notNull(wekaClassifier, "wekaClassifier");
		logger.info("training a WekaNominalClassifier using " + wekaClassifier.getClass().getCanonicalName() + " as Weka Classifier using " + predictorsTrain.getNumRows() + " instances.");
		logger.debug("Start internal masking...");
		long start = System.currentTimeMillis();
		enc = new MyArffEncoder(predictorsTrain.getNumColumns()+1);

		MatrixView matrixView = new MatrixView(new IntMatrixView(targetsTrain));

		ColumnUnionMatrixView data = new ColumnUnionMatrixView( new Matrix[] { predictorsTrain, matrixView });
		logger.debug("Converting to WEKA-Instances...");
		Instances wtrain = Matrices.wekaInstances(data,enc);
		wtrain.setClassIndex(wtrain.numAttributes()-1);

		logger.debug("Calling buildClassifier()");
		buildClassifier(wtrain);

		/*
		 * the special test data encoder is needed, as WEKA needs class label information (which is not present in subsequent calls to #predict within "our" (ISMLL) test data
		 */
		myTestdataArffEncoder = new MyTestdataArffEncoder(enc);
		myTestdataArffEncoder.map=ArffEncoderHelper.getWekaNominalMap(enc, data.getNumColumns()-1, data);
		long end = System.currentTimeMillis();
		logger.debug("Done in " + (end-start)  + " milliseconds!");
	}


	protected TreeSet<String> manualClassmap;

	Set<String> initializeClassmap() {
		if (manualClassmap == null) {
			manualClassmap = new TreeSet<String>();
		}
		return manualClassmap;
	}

	/**
	 * shall train use super.wekaClassifier on the given Instances.
	 * 
	 * @param wtrain
	 * @throws Exception
	 */
	protected abstract void buildClassifier(Instances wtrain) throws Exception;

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
		logger.info("Predicting class labels for " + predictorsTest.getNumRows() + " instances.");

		DefaultMatrix unknown = new DefaultMatrix(predictorsTest.getNumRows(), 1);
		Matrices.set(unknown, -1);

		logger.debug("Converting to WEKA-Instances");
		Instances w = Matrices.wekaInstances(new ColumnUnionMatrixView(	new Matrix[] { predictorsTest, unknown }), myTestdataArffEncoder);
		w.setClassIndex(w.numAttributes()-1);

		logger.debug("Classifying individual instances...");
		try {

			for (int i = 0; i < w.numInstances(); i++) {
				Instance currentInstance = w.instance(i);
				float prediction = (float) wekaClassifier.classifyInstance(currentInstance);
				String value = currentInstance.classAttribute().value((int) prediction);
				int convertedClassLabel = Double.valueOf(value).intValue();
				predictions.set(i,  convertedClassLabel);
			}
		} catch (Exception e) {
			throw e;
		}

		return predictions;
	}

	public void setWekaClassifier(Classifier wekaClassifier) {
		this.wekaClassifier = wekaClassifier;
	}

	public Classifier getWekaClassifier() {
		return wekaClassifier;
	}

}
