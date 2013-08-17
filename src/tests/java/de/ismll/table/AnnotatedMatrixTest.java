package de.ismll.table;

import java.io.File;
import java.io.IOException;

import de.ismll.table.Matrices.FileType;

public class AnnotatedMatrixTest {

	public static void main(String[] args) throws IOException {
		File toCheck = new File("c:/work/cluster5.dat");
		Matrix read2 = Matrices.read(toCheck, FileType.Ismll, 0);

		File annotatedMatrix = new File("c:/work/annotated1.matrix");
		AnnotatedMatrix read = (AnnotatedMatrix) Matrices.read(annotatedMatrix, FileType.AnnotatedIsmll, 1);

		System.out.println("blub".equals(read.getAnnotation("annotation1")));
		System.out.println("wrap".equals(read.getAnnotation("annotation2")));
		System.out.println("world".equals(read.getAnnotation("hello")));

		System.out.println("same? " + new MatrixTest().returnSame(read2, read));

		Matrices.writeAnnotated(read, new File("c:/work/annotated.write"));
	}

}
