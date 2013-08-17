package de.ismll.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.projections.RowSubsetMatrixView;
import de.ismll.utilities.Assert;

public class StratifiedSample {

	Logger logger = LogManager.getLogger(getClass());

	private int numRuns=20;

	private int[] columns=new int[0];

	private int maxIterations = 10000000;

	private long seed = 0;

	public static class Row{
		List<Integer> rows = new ArrayList<Integer>();
		String ident;
		@Override
		public String toString() {
			return ident + ": " + rows.toString();
		}
	}

	public RowSubsetMatrixView getStratified(final Matrix in, final Map<String, Row> group, final double[]  targetDistribution, final float percentage) throws RuntimeException{
		return getStratified(in, group, targetDistribution, percentage, true);
	}


	private RowSubsetMatrixView getStratified(final Matrix in, final Map<String, Row> in_group, final double[]  targetDistribution, final float percentage, boolean performCheck) throws RuntimeException{
		Assert.notNull(getColumns(), "columns");
		Assert.notNull(getColumns().length>0, "getColumns().length - have you set the columns to group for?");
		Assert.notNull(in, "input Matrix");
		Assert.assertTrue(percentage>=0, "percentage>=0");
		Assert.assertTrue(percentage<=1, "percentage<=1");
		logger.debug("Stratification called for matrix with " + in.getNumRows() + " rows and " + in.getNumColumns() + " cols.");

		if (performCheck) {
			Map<String, Row> testGroup = getGroupings(in);
			Assert.assertTrue(testGroup.keySet().equals(in_group.keySet()), "keys for given group and computed group are not equal according to equal()- implementation.");
			Assert.assertTrue(targetDistribution.length == testGroup.size(), "target distribution must be of same length as the given grouping/partitioning (given distribution has length " + targetDistribution.length + ", should be " + in_group.size() + "!");
			for (String key : testGroup.keySet()) {
				Row row = testGroup.get(key);
				Row compare = in_group.get(key);
				Assert.assertTrue(row.ident.equals(compare.ident), "Row-Identifier match (" + row + " and " + compare + ")");
				Assert.assertTrue(row.rows.equals(compare.rows), "Key-list on row " + row + " matches");
			}
		}

		int numRows = in.getNumRows();

		RowSubsetMatrixView ret=null;
		double bestFit = Double.MAX_VALUE;
		double[] bestProbabilities = null;
		double [] bestProbabilitiesInSample=null;

		final String rowIdents=in_group.keySet().toString();

		logger.debug("Target Probabilities: " + Arrays.toString(targetDistribution));

		for (int currentRun = 0; currentRun  <numRuns; currentRun++) {
			logger.debug("Iteration " + currentRun);
			final Map<String, Row> group = copy(in_group);

			Random rnd = new Random(currentRun);

			MonteCarlo<Row> mc = new MonteCarlo<Row>();
			mc.setSeed(getSeed() );
			{
				int i = 0;
				Collection<Row> _rowValues = group.values();
				for ( Row r : _rowValues) {
					mc.appendProbability(r, (int)(targetDistribution[i]*10000));
					i++;
				}
			}


			mc.setSeed(currentRun);
			List<Integer> inRows = new ArrayList<Integer>();
			double currentPercentage = 0.;
			int currentIteration=0;

			Iterator<Row> iterator = mc.getIterator();
			while (currentPercentage<percentage && ++currentIteration < maxIterations && iterator.hasNext() && bestFit > 0) {
				Row next = iterator.next();
				double rdn = rnd.nextDouble();
				int randomRow = (int)Math.floor(rdn*next.rows.size());
				Integer rowId = next.rows.get(randomRow);
				next.rows.remove(randomRow);
				inRows.add(rowId);

				currentPercentage=
						(double)inRows.size()/
						(double)numRows;
			}

			if (currentIteration>=maxIterations) {
				logger.warn("Exited loop: reached max iterations!");
			}
			int[] rowSubset = new int[inRows.size()];
			for (int i = 0; i < inRows.size(); i++)
				rowSubset[i] = inRows.get(i).intValue();

			logger.debug("Row Subset to be used in view: " + Arrays.toString(rowSubset));

			RowSubsetMatrixView view = new RowSubsetMatrixView(in, new DefaultIntVector(rowSubset));

			double[] probabilitiesInSample = new double[targetDistribution.length];
			int viewNumRows = view.getNumRows();
			Map<String, Row> groupSample = getGroupings(view);

			double fit = 0.;
			{
				int i = 0;
				for (Row r : groupSample.values()) {
					probabilitiesInSample[i] = (double)r.rows.size()/(double)viewNumRows;

					fit += Math.abs(targetDistribution[i]- probabilitiesInSample[i]);
					i++;
				}
				logger.debug("Probabilities in view: " + Arrays.toString(probabilitiesInSample));

			}


			if (fit < bestFit) {
				logger.info("Better fit in run " + currentRun + ": " + fit);
				bestFit=fit;
				ret = view;
				bestProbabilitiesInSample=probabilitiesInSample;
				bestProbabilities=targetDistribution;
			}
		}


		logger.info("Class identifiers:                          " + rowIdents);
		logger.info("Class probabilities in source/desired data: " + Arrays.toString(bestProbabilities));
		logger.info("Class probabilities in returned data:       " + Arrays.toString(bestProbabilitiesInSample));

		return ret;
	}


	private Map<String, Row> copy(Map<String, Row> group) {
		Map<String, Row> ret;
		if (group instanceof HashMap) {
			ret = new HashMap<String, StratifiedSample.Row>();
		} else
			if (group instanceof TreeMap) {
				ret = new TreeMap<String, StratifiedSample.Row>();
			} else {
				logger.warn("Unidentified Map type (neither TreeMap nor HashMap)! Please check Implementation in " + getClass() + ".copy() - ordering of elements might be different (created distributions might not reflect desired distributions!)");
				ret = new TreeMap<String, StratifiedSample.Row>();
			}

		for (Entry<String, Row> e : group.entrySet()) {
			ret.put(e.getKey(), copy(e.getValue()));
		}
		return ret;
	}


	private Row copy(Row value) {
		Row ret = new Row();
		ret.ident=value.ident;
		ret.rows.addAll(value.rows);
		return ret;
	}


	public RowSubsetMatrixView getStratified(final Matrix in, final float percentage) {
		Map<String, Row> group = getGroupings(in);

		double[] probabilities = calculateDistribution(in, group);

		return getStratified(in, group, probabilities, percentage, false);
	}


	public static double[] calculateDistribution(final Matrix in,
			Map<String, Row> group) {
		double[] probabilities = new double[group.size()];
		int numRows = in.getNumRows();

		int i = 0;
		for (Row r : group.values()) {
			probabilities[i] = (double)r.rows.size()/(double)numRows;
			i++;
		}
		return probabilities;
	}

	public static Map<String, Row> getGroupings(Matrix in, int[] columns) {
		int numRows = in.getNumRows();

		TreeMap<String, Row> group = new TreeMap<String, Row>();

		for (int i = 0; i < numRows; i++) {
			String rowIdent = ident(in, i, columns);
			Row row = group.get(rowIdent);
			if (row == null) {
				row = new Row();
				row.ident=rowIdent;
			}
			row.rows.add(Integer.valueOf(i));
			group.put(rowIdent, row);
		}
		return group;
	}

	private Map<String, Row> getGroupings(Matrix in) {
		return getGroupings(in, getColumns());
	}

	private static final StringBuffer sb = new  StringBuffer();

	private static String ident(Matrix in, int i, int[] columns) {
		sb.setLength(0);
		for (int j = 0; j < columns.length; j++) {
			int a = columns[j];
			if (j>0)
				sb.append(";");
			sb.append(in.get(i, a) + "");
		}
		return sb.toString();
	}

	private String ident(Matrix in, int i) {
		return ident(in, i, getColumns());
	}

	public void setNumRuns(int numRuns) {
		this.numRuns = numRuns;
	}

	public int getNumRuns() {
		return numRuns;
	}

	public void setColumns(int[] columns) {
		this.columns = columns;
	}

	public int[] getColumns() {
		return columns;
	}


	public void setSeed(long seed) {
		this.seed = seed;
	}


	public long getSeed() {
		return seed;
	}
}
