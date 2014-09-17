package de.ismll.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.ismll.bootstrap.CommandLineParser;

/**
 * 
 * TODO: Not thread safe!
 * 
 * @author Andre Busche
 *
 */
public class MessageBus {

	static public final class BroadcastStatistics {

		public boolean aborted = false;
		public String message;
		public long endTime;
		public long startTime;
		public long duration() {
			return endTime-startTime;
		}
		public int recievers;

		@Override
		public String toString() {
			return "Message Passing Statistic." + (aborted?"Message aborted. " + message:recievers + " recievers, processing took " + duration() + " millis.");
		}
	}

	public MessageBus() {
		super();
	}

	private static int nextMessageId=1;

	public static int newMessageId() {
		return nextMessageId++;
	}

	private Map<Integer, List<MessageReciever>> linking = new TreeMap<Integer, List<MessageReciever>>();

	public void subscribe(int messageType, MessageReciever m) {
		Integer key = Integer.valueOf(messageType);
		List<MessageReciever> list = linking.get(key);
		if (list==null) {
			list = new ArrayList<MessageReciever>();
			linking.put(key, list);
		}
		list.add(m);

	}

	public BroadcastStatistics broadcast(TypedMessage m) {
		return broadcast(m.getMessageType(), m);
	}

	public BroadcastStatistics broadcast(int messageType, Message m) {
		BroadcastStatistics ret = new BroadcastStatistics();
		Integer key = Integer.valueOf(messageType);
		List<MessageReciever> list = linking.get(key);
		if (list == null) {
			return ret;
		}

		boolean aborted=false;
		for (MessageReciever mr : list) {
			String reason = mr.prepareFor(messageType);
			if (reason != null) {
				ret.aborted =true;
				ret.message=reason;
				aborted=true;
				return ret;
			}
		}

		ret.startTime = System.currentTimeMillis();
		for (MessageReciever mr : list) {
			mr.process(messageType, m);
		}
		ret.endTime = System.currentTimeMillis();
		ret.recievers = list.size();

		return ret;
	}

	public void shutdown() {
		for (Entry<Integer, List<MessageReciever>> e : linking.entrySet()) {
			List<MessageReciever> value = e.getValue();
			if (value == null) continue;
			value.clear();
		}

		linking.clear();
	}

	/**
	 * decodes from the given message m the property of the given key and converts the value to the given clazz type
	 * 
	 * @param m
	 * @param keyGeometricX1
	 * @param class1
	 * @return null iff the key does not exist.
	 */
	public static <T> T decode(Message m, String key,
			Class<T> clazz) {
		Object data = m.get(key);
		if(data == null) return null;

		return (T) CommandLineParser.convert(data, clazz);
	}

	public void unsubscribe(int event,
			MessageReciever reciever) {
		// TODO Auto-generated method stub
		System.err.println("Not implemented.");
	}


}
