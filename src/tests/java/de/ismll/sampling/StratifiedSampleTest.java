package de.ismll.sampling;

import java.util.Map;

import org.junit.Test;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.MatrixTest;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.projections.RowSubsetMatrixView;

public class StratifiedSampleTest  extends MatrixTest{

	//	@Test
	public void testStratification1() {
		DefaultMatrix m1 = super.createSparseMatrix(10, 1000, 0, new int[] {0,1,2}, new int[] {3,2,2});

		StratifiedSample s = new StratifiedSample();
		//		s.logger.setLevel(Level.DEBUG);
		s.setColumns(new int[] {0,1});
		s.setNumRuns(1000);
		RowSubsetMatrixView stratified2 = s.getStratified(m1, 0.02f);
		Matrix stratified = stratified2;
		System.out.println(Matrices.toString(stratified));
		System.out.println(stratified2.getIndex());
	}

	@Test
	public void testStratification2() {
		DefaultMatrix m1 = super.createSparseMatrix(10, 1000, 0, new int[] {0,1,2}, new int[] {3,2,2});

		StratifiedSample s = new StratifiedSample();
		//		s.logger.setLevel(Level.DEBUG);
		int[] cols = new int[] {0,1};
		s.setColumns(cols);
		s.setNumRuns(10000);
		Map<String, StratifiedSample.Row> groupings = StratifiedSample.getGroupings(m1, cols);

		double[] dist = new double[] {0.0,0.0,0.5,0.2,0.2,0.1};
		RowSubsetMatrixView stratified = s.getStratified(m1, groupings, dist , 0.02f);
		System.out.println(Matrices.toString(stratified));
		System.out.println(stratified.getIndex());
	}

}
