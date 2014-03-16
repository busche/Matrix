package de.ismll.storage;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class LogStorage implements StorageTarget {

	private Logger log;
	private Priority logPriority;

	public LogStorage(String clazzIdentifier) throws ClassNotFoundException {
		super();
		Class<?> forName = Class.forName(clazzIdentifier);
		log = LogManager.getLogger(forName);

	}

	@Override
	public boolean store(Map<String, Object> values) throws StorageException {
		for (Entry<String, Object> e : values.entrySet()) {
			log.log(logPriority,e.getKey() + " -> " + e.getValue().toString());
		}
		return true;
	}

	public Priority getLogPriority() {
		return logPriority;
	}

	public void setLogPriority(Priority logPriority) {
		this.logPriority = logPriority;
	}

}
