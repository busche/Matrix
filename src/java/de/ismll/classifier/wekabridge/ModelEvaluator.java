package de.ismll.classifier.wekabridge;

import de.ismll.bootstrap.BootstrapAssertions;
import de.ismll.bootstrap.Parameter;
import de.ismll.evaluation.ClassNormalizedAccuracy;
import de.ismll.experimental.SinkTest;
import de.ismll.table.IntVector;
import de.ismll.table.Matrix;
import de.ismll.table.dataset.CategoricalDatasetProvider;
import de.ismll.table.dataset.DatasetProvider;
import de.ismll.table.dataset.DatasetProviderFactory;

public class ModelEvaluator implements Runnable {

	@Parameter(cmdline="dataset")
	private
	DatasetProviderFactory dataset;

	@Parameter(cmdline="classifier")
	private
	DeserializedWekaClassifier classifier;

	@Parameter(cmdline="reporter")
	private
	SinkTest reporter;


	@Override
	public void run() {
		BootstrapAssertions.notNull(this, classifier, "classifier");
		BootstrapAssertions.notNull(this, dataset, "dataset");
		BootstrapAssertions.notNull(this, reporter, "reporter");

		DatasetProvider p = dataset.getTarget();

		if (!(p instanceof CategoricalDatasetProvider)) {
			BootstrapAssertions.assertTrue(false, "Implementation only working for categorical Datasets!");
			return;
		}

		CategoricalDatasetProvider p_ = (CategoricalDatasetProvider)p;

		Matrix testData = p.getTestData();
		IntVector labels = p_.getTestLabels();

		classifier.setProgressreporter(1000);

		IntVector predict;
		try {
			predict = classifier.predict(testData);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		ClassNormalizedAccuracy cna = new ClassNormalizedAccuracy();
		cna.setReporter(reporter);
		float evaluate = cna.evaluate(labels, predict);

		System.out.println(evaluate);



	}


	public DatasetProviderFactory getDataset() {
		return dataset;
	}


	public void setDataset(DatasetProviderFactory dataset) {
		this.dataset = dataset;
	}


	public DeserializedWekaClassifier getClassifier() {
		return classifier;
	}


	public void setClassifier(DeserializedWekaClassifier classifier) {
		this.classifier = classifier;
	}


	public SinkTest getReporter() {
		return reporter;
	}


	public void setReporter(SinkTest reporter) {
		this.reporter = reporter;
	}

}