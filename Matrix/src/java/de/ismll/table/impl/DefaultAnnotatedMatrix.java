package de.ismll.table.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;

import de.ismll.table.AnnotatedMatrix;
import de.ismll.table.AnnotatedMatrixCallback;

public class DefaultAnnotatedMatrix extends DefaultMatrix implements
AnnotatedMatrix {


	public static final class DefaultAnnotatedMatrixParser implements AnnotatedMatrixCallback {

		public DefaultAnnotatedMatrix m ;

		public DefaultAnnotatedMatrixParser() {
			super();
		}

		public void setField(int row, int col, String string) {
			if (string.contains("."))
				m.set(row, col, Float.parseFloat(string));
			else
				m.set(row, col, Integer.parseInt(string));
		}

		public void meta(int numRows, int numColumns) {
			m= new DefaultAnnotatedMatrix(numRows, numColumns);
		}

		@Override
		public void annotation(String annotationLine) {
			if (annotationLine.startsWith("#"))
				annotationLine=annotationLine.substring(1);
			String[] split = annotationLine.split("=", 2);
			m.putAnnotation(split[0], split[1]);
		}

		@Override
		public void numAnnotations(int amount) {
			m.initAnnotations(amount);
		}
	}

	private TreeMap<String, String> annotations;

	public DefaultAnnotatedMatrix(int numRows, int numColumns) {
		super(numRows, numColumns);
		init();
	}

	public static DefaultAnnotatedMatrix wrap(float[][] array) {
		return new DefaultAnnotatedMatrix(array);
	}

	public void initAnnotations(int amount) {
		// ignore for now.
	}

	public DefaultAnnotatedMatrix(AnnotatedMatrix copyFromAnother) {
		super(copyFromAnother);
		init();
		copyAnnotations(copyFromAnother);
	}

	public DefaultAnnotatedMatrix(float[][] array) {
		super(array);
		init();
	}

	private void init() {
		annotations = new TreeMap<String, String>();
	}

	private void copyAnnotations(AnnotatedMatrix copyFromAnother) {
		for (String key : copyFromAnother.getAnnotationKeys())
			putAnnotation(key, copyFromAnother.getAnnotation(key));
	}

	@Override
	public Collection<String> getAnnotationKeys() {
		return Collections.unmodifiableCollection(annotations.keySet());
	}

	@Override
	public String getAnnotation(String key) {
		return annotations.get(key);
	}

	@Override
	public String putAnnotation(String key, String value) {
		return annotations.put(key, value);
	}

}
