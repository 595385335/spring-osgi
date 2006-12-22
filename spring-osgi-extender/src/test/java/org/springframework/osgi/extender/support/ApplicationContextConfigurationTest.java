/*
 * Copyright 2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.osgi.extender.support;

import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;


/**
 * Test that given a bundle, we can correctly determine the spring
 * configuration required for it.
 * 
 * @author Adrian Colyer
 */
public class ApplicationContextConfigurationTest extends TestCase {

	private static final String[] META_INF_SPRING_CONTENT = 
		new String[] {"file://META-INF/spring/context.xml","file://META-INF/spring/context-two.xml"};

	public void testBundleWithNoHeaderAndNoMetaInfSpringResourcesIsNotSpringPowered() {
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(null);
		aBundle.setResultsToReturnOnNextCallToFindEntries(null);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertFalse("bundle is not spring powered",config.isSpringPoweredBundle());
	}
	
	public void testBundleWithSpringResourcesAndNoHeaderIsSpringPowered() {
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(null);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle is spring powered",config.isSpringPoweredBundle());
	}
	
	public void testBundleWithHeaderAndNoMetaInfResourcesIsSpringPowered() {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context","META-INF/spring/context.xml");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(null);
		aBundle.setEntryReturnOnNextCallToGetEntry("file://META-INF/spring/context.xml");
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle is spring powered",config.isSpringPoweredBundle());
	}
	
	public void testBundleWithHeaderWithBadEntriesAndNoMetaInfResourcesIsNotSpringPowered() {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context","META-INF/splurge/context.xml");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(null);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertFalse("bundle is not spring powered",config.isSpringPoweredBundle());		
	}
	
	public void testBundleWithNoHeaderShouldWaitForDependencies() {
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(null);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle should wait for dependencies",config.waitForDependencies());		
	}
	
	public void testBundleWithWaitTrueShouldWaitForDependencies() {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context","*;wait-for-dependencies:=true");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle should wait for dependencies",config.waitForDependencies());				
	}
	
	public void testBundleWithWaitFalseShouldNotWaitForDependencies() {
		// *;flavour
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context","*;wait-for-dependencies:=false");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertFalse("bundle should not wait for dependencies",config.waitForDependencies());				

		// at end of list
		headers = new Hashtable();
		headers.put("Spring-Context","META-INF/spring/context.xml,*;wait-for-dependencies:=false");
		aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		aBundle.setEntryReturnOnNextCallToGetEntry("file://META-INF/spring/context.xml");
		config = new ApplicationContextConfiguration(aBundle);
		assertFalse("bundle should not wait for dependencies",config.waitForDependencies());
		
		// in middle of list
		headers = new Hashtable();
		headers.put("Spring-Context","META-INF/spring/context.xml;wait-for-dependencies:=false,*");
		aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		aBundle.setEntryReturnOnNextCallToGetEntry("file://META-INF/spring/context.xml");
		config = new ApplicationContextConfiguration(aBundle);
		assertFalse("bundle should not wait for dependencies",config.waitForDependencies());
	}
	
	public void testConfigLocationsInMetaInfNoHeader() {
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(null);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		String[] configFiles = config.getConfigurationLocations();
		assertEquals("2 config files",2,configFiles.length);
		assertEquals("bundle-url:file://META-INF/spring/context.xml",configFiles[0]);
		assertEquals("bundle-url:file://META-INF/spring/context-two.xml",configFiles[1]);
	}
	
	public void testConfigLocationsInMetaInfWithHeader() {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context","META-INF/spring/context.xml");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		aBundle.setEntryReturnOnNextCallToGetEntry("file://META-INF/spring/context.xml");
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		String[] configFiles = config.getConfigurationLocations();
		assertEquals("1 config file",1,configFiles.length);
		assertEquals("bundle:META-INF/spring/context.xml",configFiles[0]);
	}
	
	public void testConfigLocationsInMetaInfWithWildcardHeader() {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context","*;wait-for-dependencies:=false");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		String[] configFiles = config.getConfigurationLocations();		
		assertEquals("2 config files",2,configFiles.length);
		assertEquals("bundle-url:file://META-INF/spring/context.xml",configFiles[0]);
		assertEquals("bundle-url:file://META-INF/spring/context-two.xml",configFiles[1]);
	}
	
	
	public void testHeaderWithWildcardEntryAndNoMetaInfResources() {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context","*;wait-for-dependencies:=false");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(null);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertFalse("not spring powered",config.isSpringPoweredBundle());
	}
	
	public void testHeaderWithBadEntry() {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context","META-INF/spring/context-two.xml,META-INF/splurge/context.xml,");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setEntryReturnOnNextCallToGetEntry("file://META-INF/spring/context-two.xml");
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle is not spring powered",config.isSpringPoweredBundle());		
		String[] configFiles = config.getConfigurationLocations();
		assertEquals("1 config file",1,configFiles.length);
		assertEquals("bundle:META-INF/spring/context-two.xml",configFiles[0]);
	}
}