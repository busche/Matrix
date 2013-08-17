package de.ismll.table;

public interface AnnotatedMatrixCallback extends MatrixCallback{

	public void annotation(String annotationLine);

	public void numAnnotations(int amount);
}
