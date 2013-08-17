package de.ismll.table.dataset;

import java.io.File;
import java.io.IOException;

import de.ismll.bootstrap.BootstrapException;
import de.ismll.bootstrap.Parameter;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrices.FileType;
import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.projections.ColumnSubsetIntVectorView;
import de.ismll.table.projections.ColumnSubsetMatrixView;

/**
 * 
 * @author Andre Busche
 * 
 */
public class CsvPredictOnlyDatasetProvider extends
AbstractPredictOnlyDatasetProvider implements PredictOnlyDataset {

	@Parameter(cmdline = "clazz")
	protected int classColumn;

	@Parameter(cmdline = "train")
	private File trainCsv;

	@Parameter(cmdline = "test")
	private File testCsv;

	public File getTrainCsv() {
		return trainCsv;
	}

	@Override
	public Matrix getTrainingData() {
		if (super.trainingData != null)
			return super.getTrainingData();
		initTrainingData();
		return trainingData;
	}

	@Override
	public IntVector getTrainingLabels() {
		if (super.trainingLabels != null)
			return super.getTrainingLabels();
		initTrainingData();
		return trainingLabels;
	}

	@Override
	public Matrix getTestData() {
		if (super.testData != null)
			return super.getTestData();
		initTestData();
		return testData;
	}

	@Override
	public IntVector getTestLabels() {
		if (super.testLabels != null)
			return super.getTestLabels();
		initTestData();
		return testLabels;
	}

	private void initTestData() {

		Matrix dataMatrix;
		try {
			dataMatrix = Matrices.read(testCsv, FileType.Ismll, 10000);
		} catch (IOException e) {
			throw new BootstrapException("Failed to read from " + testCsv
					+ " assuming an ismll-format.");
		}

		testData = ColumnSubsetMatrixView.create(dataMatrix,
				new DefaultIntVector(new int[] { classColumn }));
		testLabels = new ColumnSubsetIntVectorView(dataMatrix, classColumn);

		log.info("Test data has " + testData.getNumRows() + " rows and "
				+ testData.getNumColumns() + " columns.");

	}

	private void initTrainingData() {

		Matrix dataMatrix;
		try {
			dataMatrix = Matrices.read(trainCsv, FileType.Ismll, 10000);
		} catch (IOException e) {
			throw new BootstrapException("Failed to read from " + testCsv
					+ " assuming an ismll-format.");
		}

		trainingData = ColumnSubsetMatrixView.create(dataMatrix,
				new DefaultIntVector(new int[] { classColumn }));
		trainingLabels = new ColumnSubsetIntVectorView(dataMatrix, classColumn);

		log.info("Training data has " + trainingData.getNumRows()
				+ " rows and " + trainingData.getNumColumns() + " columns.");
	}

	public void setTrainCsv(File trainCsv) {
		this.trainCsv = trainCsv;
	}

	public File getTestCsv() {
		return testCsv;
	}

	public void setTestCsv(File testCsv)  {
		this.testCsv = testCsv;

	}

	@Override
	public int getClassColumn() {
		return classColumn;
	}

	public void setClassColumn(int classColumn) {
		this.classColumn = classColumn;
	}

}
