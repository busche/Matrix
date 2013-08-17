package de.ismll.messaging;

import java.util.HashMap;
import java.util.Map;

public class DefaultMessage implements Message {

	public DefaultMessage() {
		super();
		data = new HashMap<String, Object>();
	}

	public DefaultMessage(Object source) {
		this();
		this.setSource(source);
	}

	private Map<String, Object> data;

	private Object source;

	public Object putPayload(String key, Object o) {
		return data.put(key, o);
	}

	@Override
	public Map<String, ?> getData() {
		return data;
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	@Override
	public Object get(String key) {
		return data.get(key);
	}

}
