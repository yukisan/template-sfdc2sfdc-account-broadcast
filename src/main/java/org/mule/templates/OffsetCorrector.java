package org.mule.templates;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.javatuples.Pair;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import org.mule.DefaultMuleEvent;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.construct.FlowConstructAware;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Callable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.processor.MessageProcessor;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;


/*
 * TODO - Add proper documentation
 */
public class OffsetCorrector implements MuleContextAware, FlowConstructAware, Callable {

	private static final Logger logger = Logger.getLogger(OffsetCorrector.class);

	// Milliseconds that the target system is ahead of the source system
	private int delta;
	
	// ISO standard format for datetime is yyyy-MM-dd'T'HH:mm:ss.SSSZZ
	private DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	
	// Amount of systems times to consider when calculating the delta
	private int timeMeasureAttempts;
	
	private MuleContext muleContext;
	private FlowConstruct flowConstruct;
	
//	private static SubflowInterceptingChainLifecycleWrapper getSystemsTime;
	private static final String GET_SYSTEMS_TIME_FLOW_NAME = "getSystemsTime";
	

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
	
	public void updateDelta() throws InitialisationException {
		List<Pair<String, String>> timeValues = getSystemTimeValuesList();
		List<Integer> deltas = calculateDeltaForEachPair(timeValues);
		delta = weightedMean(deltas);
	}
	
	public List<Pair<String, String>> getSystemTimeValuesList() throws InitialisationException {
		SubflowInterceptingChainLifecycleWrapper subflow = getSubFlow(GET_SYSTEMS_TIME_FLOW_NAME);
		subflow.initialise();
		
		List<Pair<String, String>> timeValues = new ArrayList<Pair<String,String>>();
		for (int i = 0; i < timeMeasureAttempts; i++) {
//			invokeFlow("", null);
		}
		return timeValues;
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
	
	protected SubflowInterceptingChainLifecycleWrapper getSubFlow(String flowName) {
	    return (SubflowInterceptingChainLifecycleWrapper) muleContext.getRegistry().lookupObject(flowName);
	}
	
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();

		Validate.notEmpty((String) message.getInvocationProperty("flowName"), "The flowName should not be null nor empty.");
		String flowName = (String) message.getInvocationProperty("flowName");

		return this.invokeFlow(flowName, message);
	}

	private Object invokeFlow(String flowName, MuleMessage message) throws MuleException {
		Validate.notEmpty(flowName, "The flow name should not be null nor empty.");
		MessageProcessor targetFlow = muleContext.getRegistry().lookupObject(flowName);

		MuleEvent muleEvent = new DefaultMuleEvent(message, MessageExchangePattern.REQUEST_RESPONSE, flowConstruct);

		logger.info("About to call flow: " + flowName + "...");

		MuleEvent resultEvent = targetFlow.process(muleEvent);

		logger.info("Flow: " + flowName + " has been executed.");

		return resultEvent.getMessage().getPayload();
	}
	

	public void setMuleContext(final MuleContext muleContext) {
		this.muleContext = muleContext;
	}

	public void setFlowConstruct(final FlowConstruct flowConstruct) {
		this.flowConstruct = flowConstruct;
	}
	
	public int getDelta() {
		return delta;
	}

	public void setDelta(int delta) {
		this.delta = delta;
	}

}
