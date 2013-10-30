package de.ismll.storage;

import java.util.Map;

public class InMemoryStorage implements StorageTarget{

	private Map<String, Object> values;

	@Override
	public boolean store(Map<String, Object> values) throws StorageException {
		this.values = values;
		return true;
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

}
