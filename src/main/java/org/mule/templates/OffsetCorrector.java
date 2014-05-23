package org.mule.templates;

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
		// TODO Auto-generated method stub
		return null;
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
