package de.ismll.messaging;

public class DefaultTypedMessage extends DefaultMessage implements TypedMessage{

	private final int type;

	public DefaultTypedMessage(Object source, int type) {
		super(source);
		this.type = type;
	}

	@Override
	public int getMessageType() {
		return type;
	}
}
