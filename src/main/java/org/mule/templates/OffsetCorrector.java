package org.mule.templates;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
//import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;

public class OffsetCorrector {

	// Milliseconds that the target system is ahead of the source system
	private int delta;
	
	// ISO standard format for datetime is yyyy-MM-dd'T'HH:mm:ss.SSSZZ
	private DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	
//	private static SubflowInterceptingChainLifecycleWrapper getSystemTimeFromAFlow;
//	private static SubflowInterceptingChainLifecycleWrapper getSystemTimeFromBFlow;
	

	public String correct(String inputDate) {
		DateTime date = fmt.parseDateTime(inputDate);
		date = date.withZone(DateTimeZone.UTC);
		
		String updatedDate;
		if(delta > 0) {
			updatedDate = date.plusMillis(delta).toString(fmt);
		} else {
			updatedDate = date.minusMillis(Math.abs(delta)).toString(fmt);
		}
		return updatedDate;
	}
	
	public int updateDelta() {
		List<Pair<String, String>> timeValues = getSystemTimeValuesList();
		List<Integer> deltas = calculateDeltaForEachPair(timeValues);
		return weightedMean(deltas);
	}
	
	public List<Pair<String, String>> getSystemTimeValuesList() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Integer> calculateDeltaForEachPair(
			List<Pair<String, String>> timeValues) {
		List<Integer> deltas = new ArrayList<Integer>(); 
		for (Pair<String, String> times : timeValues) {
			DateTime date1 = fmt.parseDateTime(times.getValue0());
			date1 = date1.withZone(DateTimeZone.UTC);
			
			DateTime date2 = fmt.parseDateTime(times.getValue1());
			date2 = date2.withZone(DateTimeZone.UTC);
			
			deltas.add((int) (date1.getMillis() - date2.getMillis()));
		}
		return deltas;
	}

	public int weightedMean(List<Integer> deltas) {
		int weightedMean = 0;
		int counter = 0;
		for (Integer delta : deltas) {
			weightedMean += delta;
			counter += 1;
		}
		// The division does down rounding 		
		return (weightedMean / counter);
	}
	
	public int getDelta() {
		return delta;
	}

	public void setDelta(int delta) {
		this.delta = delta;
	}

}
