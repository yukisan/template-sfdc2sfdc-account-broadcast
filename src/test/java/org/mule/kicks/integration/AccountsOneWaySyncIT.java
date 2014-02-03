package org.mule.kicks.integration;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.transport.NullPayload;

import com.sforce.soap.partner.SaveResult;

/**
 * The objective of this class is to validate the correct behavior of the Mule Kick that make calls to external systems.
 * 
 * @author miguel.oliva
 */
public class AccountsOneWaySyncIT extends AbstractKickTestCase {
	private static final String KICK_NAME = "accountsonewaysync";
	private static final String POLL_FLOW_NAME = "triggerFlow";

	private static SubflowInterceptingChainLifecycleWrapper checkAccountflow;
	private static List<Map<String, Object>> createdAccountsInA = new ArrayList<Map<String, Object>>();

	@BeforeClass
	public static void beforeClass() {
		// Setting Default Watermark Expression to query SFDC with LastModifiedDate greater than ten seconds before current time
		System.setProperty("watermark.default.expression", "#[groovy: new Date(System.currentTimeMillis() - 10000).format(\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\", TimeZone.getTimeZone('UTC'))]");

		// Setting Polling Frecuency to 10 seconds period
		System.setProperty("polling.frequency", "10000");
	}

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {

		// Flow to retrieve accounts from target system after syncing
		checkAccountflow = getSubFlow("retrieveAccountFlow");
		checkAccountflow.initialise();

		// Create object in target system to be updated
		final SubflowInterceptingChainLifecycleWrapper createAccountInBFlow = getSubFlow("createAccountFlowB");
		createAccountInBFlow.initialise();

		final List<Map<String, Object>> createdAccountInB = new ArrayList<Map<String, Object>>();
		// This account should BE synced (updated) as the industry is Education, has more than 5000 Employees and the record exists in the target system
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

	@After
	public void tearDown() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);

		// Delete the created accounts in A
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("deleteAccountFromAFlow");
		flow.initialise();

		final List<Object> idList = new ArrayList<Object>();
		for (final Map<String, Object> c : createdAccountsInA) {
			idList.add(c.get("Id"));
		}
		flow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));

		// Delete the created accounts in B
		flow = getSubFlow("deleteAccountFromBFlow");
		flow.initialise();

		idList.clear();
		for (final Map<String, Object> createdAccount : createdAccountsInA) {
			final Map<String, Object> account = invokeRetrieveAccountFlow(checkAccountflow, createdAccount);
			if (account != null) {
				idList.add(account.get("Id"));
			}
		}
		flow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}

	@Test
	public void testMainFlow() throws Exception {
		System.out.println("About to run poll");

		runSchedulersOnce(POLL_FLOW_NAME);

		System.out.println("Poll runned");

		// Assert first object was not synced
		assertEquals("The account should not have been sync", null, invokeRetrieveAccountFlow(checkAccountflow, createdAccountsInA.get(0)));

		// Assert second object was not synced
		assertEquals("The account should not have been sync", null, invokeRetrieveAccountFlow(checkAccountflow, createdAccountsInA.get(1)));

		// Assert third object was created in target system
		Map<String, Object> payload = invokeRetrieveAccountFlow(checkAccountflow, createdAccountsInA.get(2));
		assertEquals("The account should have been sync", createdAccountsInA.get(2)
																			.get("Name"), payload.get("Name"));

		// Assert fourth object was updated in target system
		final Map<String, Object> fourthAccount = createdAccountsInA.get(3);
		payload = invokeRetrieveAccountFlow(checkAccountflow, fourthAccount);
		assertEquals("The account should have been sync (Name)", fourthAccount.get("Name"), payload.get("Name"));

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
