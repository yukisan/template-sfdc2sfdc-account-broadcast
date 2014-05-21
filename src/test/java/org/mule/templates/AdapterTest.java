package org.mule.templates;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class AdapterTest {
	
	Adapter adapter;
	
	@Before
	public void setUp() {
		// Initialize the component to be tested 		
		adapter = new Adapter();
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnAPositiveTimeDifferenceBetweenTheSystemsInvolved1() {
		// 1000 milliseconds = 1 second
		int delta = 1000;
		adapter.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = adapter.applyCorrection(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:12:04.000Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}

	@Test
	public void aStringDateGetsCorrectedBasedOnAPositiveTimeDifferenceBetweenTheSystemsInvolved2() {
		// 1200 milliseconds = 1.2 seconds
		int delta = 1200;
		adapter.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = adapter.applyCorrection(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:12:04.200Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnAPositiveTimeDifferenceBetweenTheSystemsInvolved3() {
		// 1234 milliseconds = 1.234 seconds
		int delta = 1234;
		adapter.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = adapter.applyCorrection(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:12:04.234Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnAPositiveTimeDifferenceBetweenTheSystemsInvolved4() {
		// 60000 milliseconds = 60 seconds = 1 minute
		int delta = 60000;
		adapter.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = adapter.applyCorrection(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:13:03.000Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnAPositiveTimeDifferenceBetweenTheSystemsInvolved5() {
		// 84000 milliseconds = 84 seconds = 1.4 minute
		int delta = 84000;
		adapter.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = adapter.applyCorrection(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:13:27.000Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnAZeroTimeDifferenceBetweenTheSystemsInvolved() {
		int delta = 0;
		adapter.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = adapter.applyCorrection(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:12:03.000Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnANegativeTimeDifferenceBetweenTheSystemsInvolved() {
		// -84000 milliseconds = -84 seconds = -1.4 minute
		int delta = -84000;
		adapter.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = adapter.applyCorrection(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:10:39.000Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
}
