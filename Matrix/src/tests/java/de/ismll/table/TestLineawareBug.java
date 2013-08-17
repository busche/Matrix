package de.ismll.table;

import java.io.IOException;

public class TestLineawareBug {

	public static void main(String[] args) throws IOException {
		//		DefaultMatrix m1 = DefaultMatrix.asMatrix(new float[][]{
		//				new float[]{0.00000000000203684f},
		//				new float[]{0.00000000000102684f},
		//				new float[]{1.462f},
		//				new float[]{0.00012396f},
		//				new float[]{0.000857f},
		//		});
		//
		//		File tmpFile = File.createTempFile("longaware", ".bug");
		//		if (tmpFile.exists())
		//			tmpFile.delete();
		//		tmpFile.deleteOnExit();
		//
		//		System.out.println("tmp-file is " + tmpFile);
		//		Matrices.write(m1, tmpFile.getAbsolutePath());
		//
		//		DefaultMatrix m1Read = DefaultMatrix.read_fast_with_bug_by_lars(tmpFile.getAbsolutePath(), 10);
		//
		//		// The following line would work...
		////		DefaultMatrix m1Read = DefaultMatrix.read(tmpFile.getAbsolutePath(), 10);
		//
		//		double threshold = 0.00000000000000001;
		//		boolean error = false;
		//		for (int i = 0; i < m1.getNumColumns(); i++){
		//			for (int j = 0; j < m1.getNumRows(); j++){
		//				if (Math.abs(m1.get(j, i)-m1Read.get(j, i))>threshold){
		//					System.err.println("Invalid value at (" + j + ", " + i + "): Original: " + m1.get(j, i) + ", Deserialized: " + m1Read.get(j, i));
		//					error = true;
		//				}
		//			}
		//		}
		//		if (error){
		//			System.err.println("File reading bug still exists!");
		//			System.exit(1);
		//		}
		//

	}
}
