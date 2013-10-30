package de.ismll.storage;

import java.util.Map;

public interface StorageTarget {

	public boolean store(Map<String, Object> values) throws StorageException;
}
