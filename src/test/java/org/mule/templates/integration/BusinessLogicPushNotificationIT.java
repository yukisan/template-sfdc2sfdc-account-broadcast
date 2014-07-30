/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.DefaultMuleMessage;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.construct.Flow;
import org.mule.context.notification.NotificationException;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;

import com.mulesoft.module.batch.BatchTestHelper;

/**
 * The objective of this class is to validate the correct behavior of the
 * Anypoint Template that make calls to external systems.
 * 
 */
@SuppressWarnings("unchecked")
public class BusinessLogicPushNotificationIT extends AbstractTemplateTestCase {
	
	private static final int TIMEOUT_MILLIS = 60;
	private BatchTestHelper helper;
	private Flow triggerPushFlow;
	private List<String> accountsToDelete = new ArrayList<String>();
	
	@BeforeClass
	public static void beforeClass() {
		System.setProperty("trigger.policy", "push");		
	}

	@AfterClass
	public static void shutDown() {
		System.clearProperty("trigger.policy");
	}

	@Before
	public void setUp() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		registerListeners();
		helper = new BatchTestHelper(muleContext);
		triggerPushFlow = getFlow("triggerPushFlow");
	}

	@After
	public void tearDown() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		deleteEntities();
	}

	@Test
	public void testMainFlow() throws Exception {
		// Execution
		String accountName = buildUniqueName();
		MuleMessage message = new DefaultMuleMessage(buildRequest(accountName), muleContext);
		MuleEvent testEvent = getTestEvent(message, MessageExchangePattern.REQUEST_RESPONSE);
		testEvent.setFlowVariable("sourceSystem", "A");
		triggerPushFlow.process(testEvent);
		
		helper.awaitJobTermination(TIMEOUT_MILLIS * 1000, 500);
		helper.assertJobWasSuccessful();

		HashMap<String, Object> account = new HashMap<String, Object>();
		account.put("Name", accountName);
		
		SubflowInterceptingChainLifecycleWrapper retrieveAccountFlow = getSubFlow("retrieveAccountFlow");
		retrieveAccountFlow.initialise();
		message = new DefaultMuleMessage(account, muleContext);
		testEvent = getTestEvent(message, MessageExchangePattern.REQUEST_RESPONSE);
		Map<String, String> accountInB = (Map<String, String>) retrieveAccountFlow.process(testEvent).getMessage().getPayload();
		
		// Assertions
		Assert.assertNotNull(accountInB);
		Assert.assertEquals("Account Names should be equals", account.get("Name"), accountInB.get("Name"));
		accountsToDelete.add(accountInB.get("Id"));
		
	}
	
	private void deleteEntities() throws MuleException, Exception {
		// Delete the created accounts in B
		SubflowInterceptingChainLifecycleWrapper deleteAccountFromBflow = getSubFlow("deleteAccountFromBFlow");
		deleteAccountFromBflow.initialise();
		deleteAccountFromBflow.process(getTestEvent(accountsToDelete, MessageExchangePattern.REQUEST_RESPONSE));
	}

	private void registerListeners() throws NotificationException {
		muleContext.registerListener(pipelineListener);
	}

	private String buildRequest(String accountName){
		String req = "";
		req += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		req += "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		req += " <soapenv:Body>";
		req += "  <notifications xmlns=\"http://soap.sforce.com/2005/09/outbound\">";
		req += "   <OrganizationId>00Dd0000000dtDqEAI</OrganizationId>";
		req += "   <ActionId>04kd0000000PCgvAAG</ActionId>";
		req += "   <SessionId xsi:nil=\"true\"/>";
		req += "   <EnterpriseUrl>https://na14.salesforce.com/services/Soap/c/30.0/00Dd0000000dtDq</EnterpriseUrl>";
		req += "   <PartnerUrl>https://na14.salesforce.com/services/Soap/u/30.0/00Dd0000000dtDq</PartnerUrl>";
		req += "   <Notification>";
		req += "    <Id>04ld000000TzMKpAAN</Id>";
		req += "    <sObject xsi:type=\"sf:Account\" xmlns:sf=\"urn:sobject.enterprise.soap.sforce.com\">";
		req += "     <sf:Id>001d000001XD5XKAA1</sf:Id>";
		req += "     <sf:AccountNumber>4564564</sf:AccountNumber>";
		req += "     <sf:AnnualRevenue>10000.0</sf:AnnualRevenue>";
		req += "     <sf:BillingCity>City</sf:BillingCity>";
		req += "     <sf:BillingCountry>Country</sf:BillingCountry>";
		req += "     <sf:BillingPostalCode>04001</sf:BillingPostalCode>";
		req += "     <sf:BillingState>State</sf:BillingState>";
		req += "     <sf:BillingStreet>Street</sf:BillingStreet>";
		req += "     <sf:CreatedById>005d0000000yYC7AAM</sf:CreatedById>";
		req += "     <sf:CreatedDate>2014-05-05T11:47:49.000Z</sf:CreatedDate>";
		req += "     <sf:CustomerPriority__c>High</sf:CustomerPriority__c>";
		req += "     <sf:Description>description ddddd</sf:Description>";
		req += "     <sf:Fax>+421995555</sf:Fax>";
		req += "     <sf:Industry>Apparel</sf:Industry>";
		req += "     <sf:IsDeleted>false</sf:IsDeleted>";
		req += "     <sf:LastModifiedById>005d0000000yYC7AAM</sf:LastModifiedById>";
		req += "     <sf:LastModifiedDate>2014-06-02T13:00:00.000Z</sf:LastModifiedDate>";
		req += "     <sf:LastReferencedDate>2014-05-19T11:02:14.000Z</sf:LastReferencedDate>";
		req += "     <sf:LastViewedDate>2014-05-19T11:02:14.000Z</sf:LastViewedDate>";
		req += "     <sf:Name>"+accountName+"</sf:Name>";
		req += "     <sf:NumberOfEmployees>5000</sf:NumberOfEmployees>";
		req += "     <sf:OwnerId>005d0000000yYC7AAM</sf:OwnerId>";
		req += "     <sf:Ownership>Public</sf:Ownership>";
		req += "     <sf:Phone>+421995555</sf:Phone>";
		req += "     <sf:PhotoUrl>/services/images/photo/001d000001XD5XKAA1</sf:PhotoUrl>";
		req += "     <sf:Rating>Hot</sf:Rating>";
		req += "     <sf:SLA__c>Gold</sf:SLA__c>";
		req += "     <sf:ShippingCity>Shipping City</sf:ShippingCity>";
		req += "     <sf:ShippingCountry>Country</sf:ShippingCountry>";
		req += "     <sf:ShippingPostalCode>04001</sf:ShippingPostalCode>";
		req += "     <sf:ShippingState>Shipping State</sf:ShippingState>";
		req += "     <sf:ShippingStreet>Shipping street</sf:ShippingStreet>";
		req += "     <sf:Site>http://www.test.com</sf:Site>";
		req += "     <sf:SystemModstamp>2014-05-19T11:02:14.000Z</sf:SystemModstamp>";
		req += "     <sf:Type>Prospect</sf:Type>";
		req += "     <sf:Website>http://www.test.com</sf:Website>";
		req += "    </sObject>";
		req += "   </Notification>";
		req += "  </notifications>";
		req += " </soapenv:Body>";
		req += "</soapenv:Envelope>";
		return req;
	}
	
	private String buildUniqueName() {
		return TEMPLATE_NAME + "-" + System.currentTimeMillis() + "Account";
	}
	
}
