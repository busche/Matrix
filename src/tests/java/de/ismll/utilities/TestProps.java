package de.ismll.utilities;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;

public class TestProps {

	@Test
	public void testSubstitution() {
		Properties simple = new Properties();
		Properties expanded;
		simple.setProperty("key1", "value1");
		simple.setProperty("key2", "${key1}");
		
		expanded = Props.expandProperties(simple);
		
		Assert.assertEquals("value1", expanded.getProperty("key1"));
		Assert.assertEquals("value1", expanded.getProperty("key2"));
		
	}
	
	@Test(expected=RuntimeException.class)	
	public void testRecursiveLimit() {
		Properties simple = new Properties();
		simple.setProperty("key1", "${key1}");
		
		Props.expandProperties(simple);
		
	}
	

	@Test
	public void testNestedSubstitution() {
		Properties simple = new Properties();
		Properties expanded;
		simple.setProperty("key1", "value1");
		simple.setProperty("key2", "${key1}");
		simple.setProperty("key3", "${key2}");
		
		expanded = Props.expandProperties(simple);
		
		Assert.assertEquals("value1", expanded.getProperty("key1"));
		Assert.assertEquals("value1", expanded.getProperty("key2"));
		Assert.assertEquals("value1", expanded.getProperty("key3"));
		
	}

	@Test
	public void testRemainingReference() {
		Properties simple = new Properties();
		Properties expanded;
		simple.setProperty("key1", "value1");
		simple.setProperty("key2", "${key_non_existing}");
		
		expanded = Props.expandProperties(simple);
		
		Assert.assertEquals("value1", expanded.getProperty("key1"));
		Assert.assertEquals("${key_non_existing}", expanded.getProperty("key2"));
		
	}

}
