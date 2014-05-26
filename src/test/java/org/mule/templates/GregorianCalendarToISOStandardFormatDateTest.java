package org.mule.templates;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.templates.transformers.GregorianCalendarToISOStandardFormatDate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class GregorianCalendarToISOStandardFormatDateTest {

	GregorianCalendarToISOStandardFormatDate transformer = new GregorianCalendarToISOStandardFormatDate();
	
	MuleMessage msg = mock(MuleMessage.class);
	
	@Test
	public void theTransformerReturnsTheExpectedValue() throws ParseException, TransformerException {
		GregorianCalendar testCalendar = new GregorianCalendar();
		testCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse("2014-05-20T14:12:03.000Z"));
		
		when(msg.getPayload()).thenReturn(testCalendar);
		
		String expected = "2014-05-20T14:12:03.000Z";
		assertEquals("khjg ", expected, transformer.transformMessage(msg, null));
	}

	@Test
	public void theTransformerReturnsTheExpectedValue2() throws ParseException, TransformerException {
		GregorianCalendar testCalendar = new GregorianCalendar();
		testCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse("2016-11-27T14:12:03.000Z"));
		
		when(msg.getPayload()).thenReturn(testCalendar);
		
		String expected = "2016-11-27T14:12:03.000Z";
		assertEquals("khjg ", expected, transformer.transformMessage(msg, null));
	}

	@Test
	public void theTransformerReturnsTheExpectedValue3() throws ParseException, TransformerException {
		GregorianCalendar testCalendar = new GregorianCalendar();
		testCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse("2016-11-27T09:00:00.123Z"));
		
		when(msg.getPayload()).thenReturn(testCalendar);
		
		String expected = "2016-11-27T09:00:00.123Z";
		assertEquals("khjg ", expected, transformer.transformMessage(msg, null));
	}
}
