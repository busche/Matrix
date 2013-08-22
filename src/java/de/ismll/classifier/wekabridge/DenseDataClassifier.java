package de.ismll.classifier.wekabridge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import weka.classifiers.trees.RandomForest;
import de.ismll.bootstrap.BootstrapException;
import de.ismll.bootstrap.Parameter;
import de.ismll.evaluation.ClassNormalizedAccuracy;
import de.ismll.experimental.MessageConsumerFactory;
import de.ismll.table.IntVector;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.dataset.CategoricalDatasetProvider;
import de.ismll.table.dataset.DatasetProvider;
import de.ismll.table.dataset.DatasetProviderFactory;
import de.ismll.utilities.Buffer;

/**
 * The name is uggs ...
 * 
 * 
 * @author Andre Busche
 *
 */
public class DenseDataClassifier implements Runnable{

	private static final String HYPERPARAMETERS = "Hyperparameters";

	private static final String WEKA_CLASSIFIER = "WekaClassifier";

	protected Logger log = LogManager.getLogger(getClass());

	@Parameter(cmdline="classifier")
	private WekaNominalClassifier classifier;

	@Parameter(cmdline="predictionsFile")
	private File predictionsFile;

	@Parameter(cmdline="model", description="where to store model parameters")
	private File modelParameters;

	@Parameter(cmdline="dataset")
	private DatasetProviderFactory provider;

	@Parameter(cmdline="performanceTarget")
	private MessageConsumerFactory performanceReporterTarget;

	@Override
	public void run() {

		log.info("Reading data ...");

		DatasetProvider dp = provider.getTarget();

		if (!(dp instanceof CategoricalDatasetProvider)) {
			throw new RuntimeException("Need a categorical dataset Provider");
		}
		CategoricalDatasetProvider provider_ = (CategoricalDatasetProvider) dp;
		Matrix trainData = dp.getTrainingData();
		Matrix testData = dp.getTestData();
		IntVector trainLabels = provider_.getTrainingLabels();
		IntVector testLabels = provider_.getTestLabels();

		log.info("Train data has " + trainData.getNumRows() + " rows and " + trainData.getNumColumns() + " columns.");

		if (testData!=null)
			log.info("Test data has " + testData.getNumRows() + " rows and " + testData.getNumColumns() + " columns.");

		if (classifier == null) {
			classifier = new WekaNominalClassifier();
			RandomForest rf = new RandomForest();
			rf.setSeed(0);
			rf.setDebug(true);
			rf.setMaxDepth(1);
			rf.setNumFeatures(2);
			rf.setNumTrees(10);
			classifier.setWekaClassifier(rf);

			log.warn("Using default classifier: " + rf.toString());
		}

		log.info("Building classifier ...");

		try {
			classifier.train(trainData, trainLabels);
		} catch (Exception e) {
			throw new BootstrapException(e);
		}

		// TODO: Change writing information to a directory, containing a strucured set of files.

		Vector hyperparameters = classifier.getHyperparameters();
		if (modelParameters != null && hyperparameters!=null) {
			log.info("Storing model parameters to " + modelParameters);

			try {
				weka.core.SerializationHelper.write(Buffer.newOutputStream(modelParameters), classifier.wekaClassifier);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//			Properties p = new Properties();
			//			p.put(HYPERPARAMETERS, Vectors.toString(hyperparameters));
			//			p.put(WEKA_CLASSIFIER, classifier.getWekaClassifier().getClass().getName());
			//
			//
			//			try (OutputStream newOutputStream = Buffer.newOutputStream(modelParameters);) {
			//				p.storeToXML(newOutputStream, new Date().toString());
			//				newOutputStream.close();
			//			} catch (IOException e) {
			//				e.printStackTrace();
			//			}
			//			try {
			//				Vectors.write(hyperparameters, modelParameters);
			//			} catch (IOException e) {
			//				e.printStackTrace();
			//			}
		}

		if (testData == null) return; // cannot proceed with performance evaluation ...

		log.info("Predicting labels...");

		IntVector predict;
		try {
			predict = classifier.predict(testData);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		log.info("Computing accuracy ...");

		ClassNormalizedAccuracy a = new ClassNormalizedAccuracy();
		a.setReporter(performanceReporterTarget);
		float evaluate = a.evaluate(testLabels, predict);

		log.info("Model accuracy: " + evaluate);

		if (predictionsFile!=null) {
			log.info("Storing predictions to " + predictionsFile);
			try {
				Vectors.write(predict, predictionsFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		log.info("Done!");
	}

	public File getPredictionsFile() {
		return predictionsFile;
	}

	public void setPredictionsFile(File predictionsFile) {
		this.predictionsFile = predictionsFile;
	}

	public File getModelParameters() {
		return modelParameters;
	}

	public void setModelParameters(File modelParameters) {
		this.modelParameters = modelParameters;
	}

	public WekaNominalClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(WekaNominalClassifier classifier) {
		this.classifier = classifier;
	}

	public DatasetProviderFactory getProvider() {
		return provider;
	}

	public void setProvider(DatasetProviderFactory provider) {
		this.provider = provider;
	}

	public MessageConsumerFactory getPerformanceReporterTarget() {
		return performanceReporterTarget;
	}

	public void setPerformanceReporterTarget(MessageConsumerFactory performanceReporterTarget) {
		this.performanceReporterTarget = performanceReporterTarget;
	}


}
