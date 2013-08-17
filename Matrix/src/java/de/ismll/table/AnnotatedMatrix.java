package de.ismll.table;

import java.util.Collection;

public interface AnnotatedMatrix extends Matrix{

	public Collection<String> getAnnotationKeys();

	public String getAnnotation(String key);

	public String putAnnotation(String key, String value);
}
