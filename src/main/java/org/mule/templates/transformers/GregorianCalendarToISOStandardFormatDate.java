package org.mule.templates.transformers;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;


public class GregorianCalendarToISOStandardFormatDate extends AbstractMessageTransformer {

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {

		GregorianCalendar calendar = (GregorianCalendar) message.getPayload();
		
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String date = timeFormat.format(calendar.getTime());

		return date;
	}

}
