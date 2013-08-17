package de.ismll.runtime;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class Timer {

	public enum Precision{
		Milliseconds, Nanoseconds, ThreadTiming
	}
	
	private class DT {
		final String what;
		final long whenStarted;
		public DT(String what) {
			super();
			this.what = what;
			this.whenStarted=getTime();
		}
		long whenEnded = -1;
	}
	
	private List<DT> events; 
	private final Precision precision;
	private final Class<?> class1;
	private final long initTime;
	private final String timerName;
	private ThreadMXBean threadMXBean;

	
	private  Timer(Class<?> class1, String timerName, Precision prec) {
		this.class1 = class1;
		this.timerName = timerName;
		precision = prec;
		if (prec == Precision.ThreadTiming) {
			threadMXBean = ManagementFactory.getThreadMXBean();
			this.initTime = 0;
		} else {
			this.initTime = getTime();
		}
		
		this.events=new ArrayList<DT>();
		events.add(new DT("Timer Constructed."));
	}
	
	private long getTime() {
		switch (precision) {
		case Milliseconds:
			return System.currentTimeMillis();
		case Nanoseconds:
			return System.nanoTime();
		case ThreadTiming:
			return threadMXBean.getCurrentThreadCpuTime();
		}
		return -1;
	}

	public Timer(Class<?> class1, String timerName) {
		this(class1, timerName, Precision.Nanoseconds);
	}

	public static Timer newTimer(Class<?> class1, String name) {
		return new Timer(class1, name);
	}

	public static Timer newTimer(Class<?> class1, String name, Precision p) {
		return new Timer(class1, name, p);
	}
	
	public static Timer newTimer(Class<?> class1) {
		return newTimer(class1, (String) null);
	}

	public static Timer newTimer(Class<?> class1, Precision p) {
		return newTimer(class1, null, p);
	}

	
	public void start(String aspect) {
		DT last = events.get(events.size()-1);
		long now = getTime();
		if (last.whenEnded == -1)
			last.whenEnded=now;
		events.add(new DT(aspect));
	}

	public void end() {
		DT last = events.get(events.size()-1);
		long now = getTime();
		last.whenEnded=now;		
	}

	final DateFormat dateTimeInstance = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
	
	public String printStatistics() {
		String ident = precision.toString();
		StringBuffer sb = new StringBuffer();
		sb.append(" Timer " + (timerName!=null?timerName + " (":"")+  class1.toString() + (timerName!=null?") ":"") + "\n");
		//sb.append("Constructed\t" + dateTimeInstance.format(new Date(initTime)) + "\n");
		long endTime = getTime();
		for (DT d : events) {
			if (d.whenEnded>0)
//				endTime=Math.max(endTime, d.whenEnded);
			endTime=d.whenEnded;
			sb.append(String.format("%1$-23s: %2$15s " + ident + "\n", d.what, "" +(d.whenEnded<0?"ongoing":(endTime-d.whenStarted)) ));			
		}
		sb.append(String.format("Time since construction: %1$15s " + ident + "\n", "" +(endTime-initTime) ));			
		sb.append(String.format("Time since first event:  %1$15s " + ident + "\n", "" +(endTime-events.get(0).whenStarted) ));
		return sb.toString();
	}
}
