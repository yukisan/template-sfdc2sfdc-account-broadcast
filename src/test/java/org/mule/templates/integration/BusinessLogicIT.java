package org.mule.templates.integration;

import static org.junit.Assert.assertEquals;
import static org.mule.templates.builders.SfdcObjectBuilder.anAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.context.notification.NotificationException;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.templates.builders.SfdcObjectBuilder;
import org.mule.templates.test.utils.ListenerProbe;

import com.mulesoft.module.batch.BatchTestHelper;
import com.sforce.soap.partner.SaveResult;

/**
 * The objective of this class is to validate the correct behavior of the
 * Anypoint Template that make calls to external systems.
 * 
 */
public class BusinessLogicIT extends AbstractTemplateTestCase {

	private BatchTestHelper helper;
	private List<Map<String, Object>> createdAccountsInA = new ArrayList<Map<String, Object>>();

	@Before
	public void setUp() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		registerListeners();

		helper = new BatchTestHelper(muleContext);

		// Flow to retrieve accounts from target system after syncing
		retrieveAccountFromBFlow = getSubFlow("retrieveAccountFlow");
		retrieveAccountFromBFlow.initialise();

		createEntities();
	}

	@After
	public void tearDown() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		deleteEntities();
	}

	@Test
	public void testMainFlow() throws Exception {
		// Run poll and wait for it to run
		runSchedulersOnce(POLL_FLOW_NAME);
		waitForPollToRun();

		// Wait for the batch job executed by the poll flow to finish
		helper.awaitJobTermination(TIMEOUT_SEC * 1000, 500);
		helper.assertJobWasSuccessful();

		// Assert first object was not sync
		assertEquals("The account should not have been sync", null, invokeRetrieveFlow(retrieveAccountFromBFlow, createdAccountsInA.get(0)));

		// Assert second object was not sync
		assertEquals("The account should not have been sync", null, invokeRetrieveFlow(retrieveAccountFromBFlow, createdAccountsInA.get(1)));

		// Assert third object was sync to target system 
		Map<String, Object> payload = invokeRetrieveFlow(retrieveAccountFromBFlow, createdAccountsInA.get(2));
		System.err.println(createdAccountsInA.get(2));
		System.err.println(payload);
		assertEquals("The account should have been sync", createdAccountsInA.get(2).get("Name"), payload.get("Name"));

		// Assert fourth object was sync to target system
		Map<String, Object> fourthAccount = createdAccountsInA.get(3);
		payload = invokeRetrieveFlow(retrieveAccountFromBFlow, fourthAccount);
		assertEquals("The account should have been sync (Name)", fourthAccount.get("Name"), payload.get("Name"));
	}

	private void registerListeners() throws NotificationException {
		muleContext.registerListener(pipelineListener);
	}

	private void waitForPollToRun() {
		pollProber.check(new ListenerProbe(pipelineListener));
	}

	@SuppressWarnings("unchecked")
	private void createEntities() throws MuleException, Exception {

		// Create object in target system to be update
		SubflowInterceptingChainLifecycleWrapper createAccountInBFlow = getSubFlow("createAccountFlowB");
		createAccountInBFlow.initialise();

		SfdcObjectBuilder updateAccount = anAccount().with("Name", buildUniqueName(TEMPLATE_NAME, "DemoUpdateAccount")).with("Industry", "Education");

		List<Map<String, Object>> createdAccountInB = new ArrayList<Map<String, Object>>();
		// This account should BE sync (updated) as the industry is Education,
		// has more than 5000 Employees and the record exists in the target
		// system
		createdAccountInB.add(updateAccount.with("NumberOfEmployees", 17000).build());
		createAccountInBFlow.process(getTestEvent(createdAccountInB, MessageExchangePattern.REQUEST_RESPONSE));

		// Create accounts in source system to be or not to be synced
		SubflowInterceptingChainLifecycleWrapper createAccountInAFlow = getSubFlow("createAccountFlowA");
		createAccountInAFlow.initialise();

		// This account should not be synced as the industry is not "Education"
		// or "Government"
		createdAccountsInA.add(anAccount().with("Name", buildUniqueName(TEMPLATE_NAME, "DemoFilterIndustryAccount")).with("Industry", "Insurance").with("NumberOfEmployees", 17000).build());

		// This account should not be synced as the number of employees is less
		// than 5000
		createdAccountsInA.add(anAccount().with("Name", buildUniqueName(TEMPLATE_NAME, "DemoFilterIndustryAccount")).with("Industry", "Government").with("NumberOfEmployees", 2500).build());

		// This account should BE synced (inserted) as the number of employees
		// if greater than 5000 and the industry is "Government"
		createdAccountsInA.add(anAccount().with("Name", buildUniqueName(TEMPLATE_NAME, "DemoCreateAccount")).with("Industry", "Government").with("NumberOfEmployees", 18000).build());

		// This account should BE synced (updated) as the number of employees if
		// greater than 5000 and the industry is "Education"
		createdAccountsInA.add(updateAccount.with("NumberOfEmployees", 12000).build());

		final MuleEvent event = createAccountInAFlow.process(getTestEvent(createdAccountsInA, MessageExchangePattern.REQUEST_RESPONSE));
		final List<SaveResult> results = (List<SaveResult>) event.getMessage().getPayload();
		int i = 0;
		for (SaveResult result : results) {
			Map<String, Object> accountInA = createdAccountsInA.get(i);
			accountInA.put("Id", result.getId());
			i++;
		}
	}

	private void deleteEntities() throws MuleException, Exception {
		// Delete the created accounts in A
		SubflowInterceptingChainLifecycleWrapper deleteAccountFromAflow = getSubFlow("deleteAccountFromAFlow");
		deleteAccountFromAflow.initialise();

		final List<Object> idList = new ArrayList<Object>();
		for (final Map<String, Object> c : createdAccountsInA) {
			idList.add(c.get("Id"));
		}
		deleteAccountFromAflow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));

		// Delete the created accounts in B
		SubflowInterceptingChainLifecycleWrapper deleteAccountFromBflow = getSubFlow("deleteAccountFromBFlow");
		deleteAccountFromBflow.initialise();

		idList.clear();
		for (final Map<String, Object> createdAccount : createdAccountsInA) {
			final Map<String, Object> account = invokeRetrieveFlow(retrieveAccountFromBFlow, createdAccount);
			if (account != null) {
				idList.add(account.get("Id"));
			}
		}
		deleteAccountFromBflow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}

}
