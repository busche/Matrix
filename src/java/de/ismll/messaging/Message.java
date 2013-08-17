package de.ismll.messaging;

import java.util.Map;

public interface Message {

	public Map<String, ?> getData();

	public Object get(String key);

	public Object getSource();
}
