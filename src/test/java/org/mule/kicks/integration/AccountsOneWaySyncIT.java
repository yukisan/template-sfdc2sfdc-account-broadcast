package org.mule.kicks.integration;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.context.notification.NotificationException;
import org.mule.kicks.test.utils.ListenerProbe;
import org.mule.kicks.test.utils.PipelineSynchronizeListener;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Prober;
import org.mule.transport.NullPayload;

import com.sforce.soap.partner.SaveResult;

/**
 * The objective of this class is to validate the correct behavior of the Mule Kick that make calls to external systems.
 * 
 * @author miguel.oliva
 */
public class AccountsOneWaySyncIT extends AbstractKickTestCase {
	private static final String POLL_FLOW_NAME = "triggerFlow";
	private static final String KICK_NAME = "accountsonewaysync";

	private static SubflowInterceptingChainLifecycleWrapper checkAccountflow;
	private static List<Map<String, Object>> createdAccountsInA = new ArrayList<Map<String, Object>>();

	private final Prober workingPollProber = new PollingProber(10000, 1000l);

	private final PipelineSynchronizeListener pipelineListener = new PipelineSynchronizeListener(POLL_FLOW_NAME);

	@Before
	public void setUp() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		registerListeners();

		// Flow to retrieve accounts from target system after syncing
		checkAccountflow = getSubFlow("retrieveAccountFlow");
		checkAccountflow.initialise();

		createEntities();
	}

	@After
	public void tearDown() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		deleteEntities();
	}

	@Test
	public void testMainFlow() throws Exception {
		runSchedulersOnce(POLL_FLOW_NAME);

		waitForPollToRun();

		// Assert first object was not sync
		assertEquals("The account should not have been sync", null, invokeRetrieveAccountFlow(checkAccountflow, createdAccountsInA.get(0)));

		// Assert second object was not sync
		assertEquals("The account should not have been sync", null, invokeRetrieveAccountFlow(checkAccountflow, createdAccountsInA.get(1)));

		// Assert third object was sync to target system
		Map<String, Object> payload = invokeRetrieveAccountFlow(checkAccountflow, createdAccountsInA.get(2));
		assertEquals("The account should have been sync", createdAccountsInA.get(2)
																			.get("Name"), payload.get("Name"));
		// Assert fourth object was sync to target system
		final Map<String, Object> fourthAccount = createdAccountsInA.get(3);
		payload = invokeRetrieveAccountFlow(checkAccountflow, fourthAccount);
		assertEquals("The account should have been sync (Name)", fourthAccount.get("Name"), payload.get("Name"));
	}

	private void registerListeners() throws NotificationException {
		muleContext.registerListener(pipelineListener);
	}

	private void resetListeners() {
		pipelineListener.resetListener();
	}

	private void waitForPollToRun() {
		System.out.println("Waiting for poll to run ones...");
		workingPollProber.check(new ListenerProbe(pipelineListener));
		System.out.println("Poll flow done");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> invokeRetrieveAccountFlow(final SubflowInterceptingChainLifecycleWrapper flow, final Map<String, Object> account) throws Exception {
		final Map<String, Object> accountMap = new HashMap<String, Object>();

		accountMap.put("Name", account.get("Name"));
		final MuleEvent event = flow.process(getTestEvent(accountMap, MessageExchangePattern.REQUEST_RESPONSE));
		final Object payload = event.getMessage()
									.getPayload();
		System.out.println("Retrieve Accounts Result for: " + account.get("Name") + " is " + payload);
		if (payload instanceof NullPayload) {
			return null;
		} else {
			return (Map<String, Object>) payload;
		}
	}

	@SuppressWarnings("unchecked")
	private void createEntities() throws MuleException, Exception {

		// Create object in target system to be update
		final SubflowInterceptingChainLifecycleWrapper createAccountInBFlow = getSubFlow("createAccountFlowB");
		createAccountInBFlow.initialise();

		final List<Map<String, Object>> createdAccountInB = new ArrayList<Map<String, Object>>();
		// This account should BE sync (updated) as the industry is Education, has more than 5000 Employees and the record exists in the target system
		createdAccountInB.add(anAccount().withProperty("Name", buildUniqueName(KICK_NAME, "DemoUpdateAccount"))
											.withProperty("Industry", "Education")
											.withProperty("NumberOfEmployees", 17000)
											.build());
		createAccountInBFlow.process(getTestEvent(createdAccountInB, MessageExchangePattern.REQUEST_RESPONSE));

		// Create accounts in source system to be or not to be synced
		final SubflowInterceptingChainLifecycleWrapper createAccountInAFlow = getSubFlow("createAccountFlowA");
		createAccountInAFlow.initialise();

		// This account should not be synced as the industry is not "Education" or "Government"
		createdAccountsInA.add(anAccount().withProperty("Name", buildUniqueName(KICK_NAME, "DemoFilterIndustryAccount"))
											.withProperty("Industry", "Insurance")
											.withProperty("NumberOfEmployees", 17000)
											.build());

		// This account should not be synced as the number of employees is less than 5000
		createdAccountsInA.add(anAccount().withProperty("Name", buildUniqueName(KICK_NAME, "DemoFilterIndustryAccount"))
											.withProperty("Industry", "Government")
											.withProperty("NumberOfEmployees", 2500)
											.build());

		// This account should BE synced (inserted) as the number of employees if greater than 5000 and the industry is "Government"
		createdAccountsInA.add(anAccount().withProperty("Name", buildUniqueName(KICK_NAME, "DemoCreateAccount"))
											.withProperty("Industry", "Government")
											.withProperty("NumberOfEmployees", 18000)
											.build());

		// This account should BE synced (updated) as the number of employees if greater than 5000 and the industry is "Education"
		createdAccountsInA.add(anAccount().withProperty("Name", buildUniqueName(KICK_NAME, "DemoUpdateAccount"))
											.withProperty("Industry", "Education")
											.withProperty("NumberOfEmployees", 12000)
											.build());

		final MuleEvent event = createAccountInAFlow.process(getTestEvent(createdAccountsInA, MessageExchangePattern.REQUEST_RESPONSE));
		final List<SaveResult> results = (List<SaveResult>) event.getMessage()
																	.getPayload();
		System.out.println("Results from creation in A" + results.toString());
		int i = 0;
		for (SaveResult result : results) {
			Map<String, Object> accountInA = createdAccountsInA.get(i);
			accountInA.put("Id", result.getId());
			i++;
		}

		System.out.println("Results after adding" + createdAccountsInA.toString());
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
			final Map<String, Object> account = invokeRetrieveAccountFlow(checkAccountflow, createdAccount);
			if (account != null) {
				idList.add(account.get("Id"));
			}
		}
		deleteAccountFromBflow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}

	// ***************************************************************
	// ======== AccountBuilder class ========
	// ***************************************************************
	private AccountBuilder anAccount() {
		return new AccountBuilder();
	}

	private static class AccountBuilder {

		private final Map<String, Object> account = new HashMap<String, Object>();

		public AccountBuilder withProperty(final String key, final Object value) {
			account.put(key, value);
			return this;
		}

		public Map<String, Object> build() {
			return account;
		}

	}

}
