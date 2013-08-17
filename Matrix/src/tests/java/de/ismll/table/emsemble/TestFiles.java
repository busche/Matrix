package de.ismll.table.emsemble;

import java.io.File;
import java.io.IOException;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;

public class TestFiles {

	//	@Test
	public void createTestFiles() {
		File baseDir = new File("h:/ensemble_files2");
		baseDir.mkdirs();

		for (int j :new int[] {0,1,2,3,4,5,6,7,8,9}) {
			File results1=new File(baseDir, "results" + j);
			results1.mkdirs();

			for (int split : new int[] {0}) {
				System.out.println(j + "-" + split);
				File output = new File(results1, "extern_algebra_" + split + ".kdd.predictions");
				Matrix randomMatrix = Matrices.getConstMatrix(100000, 2, 0);
				for (int i = 0; i < randomMatrix.getNumRows(); i++) {
					randomMatrix.set(i, 0, (i+1));
					randomMatrix.set(i, 1, (float) Math.random());
				}
				try {
					Matrices.write(randomMatrix, output);
				} catch (IOException e) {

					e.printStackTrace();
				}
			}

		}
	}
}
