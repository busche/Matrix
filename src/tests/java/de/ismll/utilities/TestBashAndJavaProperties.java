package de.ismll.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import de.ismll.utilities.Props;

public class TestBashAndJavaProperties {

	private static final String KEY_TARGET_DIR = "target_dir";

	private static final String KEY_SPECIFIC_DIR = "specific_dir";
	
	private static String GENERIC_FILENAME_STRING="/scripts/samples/general";
	private static String SPECIFIC_FILENAME_STRING="/scripts/samples/specific";
	
	@Test
	public void testGeneral() {
		Properties p = new Properties();
		
		try {
			InputStream is = getClass().getResourceAsStream(GENERIC_FILENAME_STRING);
			Assert.assertNotNull("Input Stream to Resource required, but was null.", is);
			
			p.load(is);
		} catch (IOException e) {
			Assert.fail(GENERIC_FILENAME_STRING + " should have been accessible... Got Exception: " + e.getMessage());
		}
		
		String targetDir = p.getProperty(KEY_TARGET_DIR);
		String specificDir = p.getProperty(KEY_SPECIFIC_DIR);
		Assert.assertEquals("/tmp", targetDir);
		Assert.assertNull(specificDir);	
		
	}

	
	@Test
	public void testSpecific() {
		Properties p = new Properties();
		
		try {
			InputStream isGeneric = getClass().getResourceAsStream(GENERIC_FILENAME_STRING);
			Assert.assertNotNull("Input Stream to Resource required, but was null.", isGeneric);

			InputStream isSpecific= getClass().getResourceAsStream(SPECIFIC_FILENAME_STRING);
			Assert.assertNotNull("Input Stream to Resource required, but was null.", isSpecific);
			
			p.load(isGeneric);
			p.load(isSpecific);
		} catch (IOException e) {
			Assert.fail(GENERIC_FILENAME_STRING + " should have been accessible... Got Exception: " + e.getMessage());
		}
		
		String targetDir = p.getProperty(KEY_TARGET_DIR);
		String specificDir = p.getProperty(KEY_SPECIFIC_DIR);
		Properties p2 = Props.expandProperties(p);
		String specificDirExpanded = p2.getProperty(KEY_SPECIFIC_DIR);
		Assert.assertEquals("/tmp", targetDir);
		Assert.assertEquals("${target_dir}/specific", specificDir);
		Assert.assertEquals("/tmp/specific", specificDirExpanded);
				
		System.out.println(p2.toString());
	}
}
