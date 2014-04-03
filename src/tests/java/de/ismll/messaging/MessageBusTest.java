package de.ismll.messaging;


import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.ismll.messaging.MessageBus.BroadcastStatistics;

public class MessageBusTest {

	private static final class MessageRecieverImplementation implements
	MessageReciever {
		private boolean accepts;

		public MessageRecieverImplementation(boolean accepts) {
			this.accepts = accepts;
		}

		@Override
		public void process(int messageType, Message m) {
			System.out.println("Got call: process(" + messageType + "," + m.toString() + ")");
		}

		@Override
		public String prepareFor(int messageType) {
			System.out.println("Got call: prepareFor(" + messageType + ")");
			if (!accepts) return "Geht nicht";

			return null;
		}
	}

	private static final class MessageImplementation implements Message {
		private int messageId;

		public MessageImplementation(int i) {
			this.messageId = i;
		}

		@Override
		public Map<String, ?> getData() {
			return null;
		}

		@Override
		public String toString() {
			return "Message " + messageId;
		}

		@Override
		public Object get(String key) {
			return null;
		}

		@Override
		public Object getSource() {
			return null;
		}
	}

	private MessageBus testingBus;

	@Before
	public void setUp() throws Exception {
		System.out.println("---- resetting message bus ----");
		testingBus = new MessageBus();
	}

	@After
	public void tearDown() throws Exception {
		testingBus.shutdown();
	}

	@Test
	public void testSucess() {
		MessageReciever mr1 = new MessageRecieverImplementation(true);
		testingBus.subscribe(1, mr1);
		testingBus.subscribe(2, mr1);

		Message m1 = new MessageImplementation(100);
//		Message m2 = new MessageImplementation(101);
		BroadcastStatistics broadcast1 = testingBus.broadcast(1, m1);
		System.out.println(broadcast1);
		Assert.assertNull(broadcast1.message);


	}

	@Test
	public void testAbort() {
//		MessageReciever mr1 = new MessageRecieverImplementation(true);
		MessageReciever mr2 = new MessageRecieverImplementation(false);
		testingBus.subscribe(1, mr2);

		Message m1 = new MessageImplementation(100);
		BroadcastStatistics broadcast1 = testingBus.broadcast(1, m1);
		System.out.println(broadcast1);
		Assert.assertNotNull(broadcast1.message);

	}


}
