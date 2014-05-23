package org.mule.templates;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;
//import org.mule.MessageExchangePattern;
//import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
//import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.templates.OffsetCorrector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
//import static org.mockito.Mockito.*;

public class OffsetCorrectorTest {
	
	OffsetCorrector corrector;
//	private static SubflowInterceptingChainLifecycleWrapper getSystemTimeFromAFlow;
//	private static SubflowInterceptingChainLifecycleWrapper getSystemTimeFromBFlow;
	
	@Before
	public void setUp() {
		// Initialize the component to be tested 		
		corrector = new OffsetCorrector();
		
		// Create mock objects
//		getSystemTimeFromAFlow = mock(SubflowInterceptingChainLifecycleWrapper.class);
//		getSystemTimeFromBFlow = mock(SubflowInterceptingChainLifecycleWrapper.class);
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnAPositiveTimeDifferenceBetweenTheSystemsInvolved1() {
		// 1000 milliseconds = 1 second
		int delta = 1000;
		corrector.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = corrector.correct(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:12:04.000Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}

	@Test
	public void aStringDateGetsCorrectedBasedOnAPositiveTimeDifferenceBetweenTheSystemsInvolved2() {
		// 1200 milliseconds = 1.2 seconds
		int delta = 1200;
		corrector.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = corrector.correct(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:12:04.200Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnAPositiveTimeDifferenceBetweenTheSystemsInvolved3() {
		// 1234 milliseconds = 1.234 seconds
		int delta = 1234;
		corrector.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = corrector.correct(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:12:04.234Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnAPositiveTimeDifferenceBetweenTheSystemsInvolved4() {
		// 60000 milliseconds = 60 seconds = 1 minute
		int delta = 60000;
		corrector.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = corrector.correct(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:13:03.000Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnAPositiveTimeDifferenceBetweenTheSystemsInvolved5() {
		// 84000 milliseconds = 84 seconds = 1.4 minute
		int delta = 84000;
		corrector.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = corrector.correct(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:13:27.000Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnAZeroTimeDifferenceBetweenTheSystemsInvolved() {
		int delta = 0;
		corrector.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = corrector.correct(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:12:03.000Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnANegativeTimeDifferenceBetweenTheSystemsInvolved() {
		// -84000 milliseconds = -84 seconds = -1.4 minute
		int delta = -84000;
		corrector.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000Z";
		String obtainedDate = corrector.correct(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T14:10:39.000Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
	@Test
	public void aStringDateGetsCorrectedBasedOnAPositiveTimeDifferenceBetweenTheSystemsInvolvedWithDifferentTimezone() {
		// 84000 milliseconds = 84 seconds = 1.4 minute
		int delta = 84000;
		corrector.setDelta(delta);
		
		String someBLastUpdatedDate = "2014-05-20T14:12:03.000-03:00";
		String obtainedDate = corrector.correct(someBLastUpdatedDate);
		
		String expectedDate = "2014-05-20T17:13:27.000Z";
		assertEquals("The date obtained is not the same one as the one expected", expectedDate, obtainedDate); 
	}
	
	@Test
	public void theDeltaIsTheWeightedMeanOfTheListOfDeltasWithWeightOne1() throws Exception {
		List<Integer> deltas = new ArrayList<Integer>();
		deltas.add(5000);
		deltas.add(5000);
		deltas.add(5000);
		
		assertEquals("The delta obtained is not the same one as the one expected", 5000, corrector.weightedMean(deltas));	
	}

	@Test
	public void theDeltaIsTheWeightedMeanOfTheListOfDeltasWithWeightOne2() throws Exception {
		List<Integer> deltas = new ArrayList<Integer>();
		deltas.add(84000);
		
		assertEquals("The delta obtained is not the same one as the one expected", 84000, corrector.weightedMean(deltas));	
	}
	
	@Test
	public void theDeltaIsTheWeightedMeanOfTheListOfDeltasWithWeightOne3() throws Exception {
		List<Integer> deltas = new ArrayList<Integer>();
		deltas.add(5500);
		deltas.add(5000);
		deltas.add(4500);
		
		assertEquals("The delta obtained is not the same one as the one expected", 5000, corrector.weightedMean(deltas));	
	}
	
	@Test
	public void theDeltaIsTheWeightedMeanOfTheListOfDeltasWithWeightOne4() throws Exception {
		List<Integer> deltas = new ArrayList<Integer>();
		deltas.add(5500);
		deltas.add(5000);
		deltas.add(4500);
		
		assertEquals("The delta obtained is not the same one as the one expected", 5000, corrector.weightedMean(deltas));	
	}
	
	@Test
	public void theDeltaIsTheWeightedMeanOfTheListOfDeltasWithWeightOne5() throws Exception {
		List<Integer> deltas = new ArrayList<Integer>();
		deltas.add(1);
		deltas.add(2);
		deltas.add(3);
		deltas.add(4);
		deltas.add(5);
		
		assertEquals("The delta obtained is not the same one as the one expected", 3, corrector.weightedMean(deltas));	
	}
	
	@Test
	public void theDeltaIsTheWeightedMeanOfTheListOfDeltasWithWeightOne6() throws Exception {
		List<Integer> deltas = new ArrayList<Integer>();
		deltas.add(1);
		deltas.add(2);
		deltas.add(3);
		deltas.add(3);
		deltas.add(4);
		deltas.add(5);
		
		assertEquals("The delta obtained is not the same one as the one expected", 3, corrector.weightedMean(deltas));	
	}
	
	@Test
	public void theDeltaIsTheWeightedMeanOfTheListOfDeltasWithWeightOne7() throws Exception {
		List<Integer> deltas = new ArrayList<Integer>();
		deltas.add(1);
		deltas.add(2);
		deltas.add(3);
		deltas.add(3);
		deltas.add(3);
		deltas.add(4);
		
		assertEquals("The delta obtained is not the same one as the one expected", 2, corrector.weightedMean(deltas));	
	}
	
	@Test
	public void forEveryPairOfTimeValuesGivenTheResultHasADeltaThatRepresentsTheDifferenceBetweenThem() {
		List<Pair<String, String>> timeValues = new ArrayList<Pair<String, String>>();
		timeValues.add(new Pair<String, String>("2014-05-20T14:12:03.000-03:00", "2014-05-20T14:12:03.000-03:00"));
		timeValues.add(new Pair<String, String>("2014-05-20T14:12:03.000-03:00", "2014-05-20T14:13:03.000-03:00"));
		timeValues.add(new Pair<String, String>("2014-05-20T14:12:03.000-03:00", "2014-05-20T14:11:05.000-03:00"));
		timeValues.add(new Pair<String, String>("2014-05-20T14:12:03.000-03:00", "2014-05-20T14:30:00.123-03:00"));
		
		assertTrue("The delta time between 2014-05-20T14:12:03.000-03:00 and 2014-05-20T14:12:03.000-03:00 is not included in the output list", 
				corrector.calculateDeltaForEachPair(timeValues).contains(0));	
		assertTrue("The delta time between 2014-05-20T14:12:03.000-03:00 and 2014-05-20T14:13:03.000-03:00 is not included in the output list", 
				corrector.calculateDeltaForEachPair(timeValues).contains(-60000));	
		assertTrue("The delta time between 2014-05-20T14:12:03.000-03:00 and 2014-05-20T14:12:01.000-03:00 is not included in the output list", 
				corrector.calculateDeltaForEachPair(timeValues).contains(58000));	
		assertTrue("The delta time between 2014-05-20T14:12:03.000-03:00 and 2014-05-20T14:30:00.123-03:00 is not included in the output list", 
				corrector.calculateDeltaForEachPair(timeValues).contains(-1077123));	
		
		assertTrue("The output list includes more elements than expected", corrector.calculateDeltaForEachPair(timeValues).size() == 4);	
	}

	//	@Test
//	public void theDeltaIsBeingProperlyUpdated() throws MuleException, Exception {
//		String systemTimeInA1 = "2014-05-20T14:12:03.000Z";
//		String systemTimeInA2 = "2014-05-20T14:12:08.000Z";
//		String systemTimeInA3 = "2014-05-20T14:12:11.000Z";
//		when(getSystemTimeFromAFlow.process(FunctionalTestCase.getTestEvent(null, MessageExchangePattern.REQUEST_RESPONSE)).getMessage().getPayload())
//		.thenReturn(systemTimeInA1)
//		.thenReturn(systemTimeInA2)
//		.thenReturn(systemTimeInA3);
//		
//		String systemTimeInB1 = "2014-05-20T14:12:03.000Z";
//		String systemTimeInB2 = "2014-05-20T14:12:07.500Z";
//		String systemTimeInB3 = "2014-05-20T14:12:11.500Z";
//		when(getSystemTimeFromBFlow.process(FunctionalTestCase.getTestEvent(null, MessageExchangePattern.REQUEST_RESPONSE)).getMessage().getPayload())
//		.thenReturn(systemTimeInB1)
//		.thenReturn(systemTimeInB2)
//		.thenReturn(systemTimeInB3);
//		
//		int expected = 0;
//		int actual = adapter.updateDelta();
//		assertEquals("Something did not went as expected when calculating and updaing the value of the delta", expected, actual);
//		
//		
//	}
	

}
