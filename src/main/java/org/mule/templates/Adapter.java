package org.mule.templates;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class Adapter {

	// Milliseconds that the target system is ahead of the source system
	private int delta;
	
	// ISO standard format for datetime is yyyy-MM-dd'T'HH:mm:ss.SSSZZ
	private DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	
	
	public String applyCorrection(String dateToBeUpdated) {
		DateTime date = fmt.parseDateTime(dateToBeUpdated);
		date = date.withZone(DateTimeZone.UTC);
		
		String updatedDate;
		if(delta > 0) {
			updatedDate = date.plusMillis(delta).toString(fmt);
		} else {
			updatedDate = date.minusMillis(Math.abs(delta)).toString(fmt);
		}
		return updatedDate;
	}
	
	public int getDelta() {
		return delta;
	}

	public void setDelta(int delta) {
		this.delta = delta;
	}

}
