package com.caid.utopia.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.caid.utopia.UtopiaAccountApplicationTests;
import com.caid.utopia.entity.Account;
import com.caid.utopia.entity.AccountRole;

@TestInstance(value = Lifecycle.PER_CLASS)
public class AccountControllerTests extends UtopiaAccountApplicationTests {

	Logger logger = LoggerFactory.getLogger(AccountControllerTests.class);

	private Account loggedInAdmin, loggedInUser;
	private HttpHeaders jwtMockHeaders;
	private MockRestServiceServer mockServer;

	@Autowired
	private RestTemplate restTemplate;

	@BeforeEach
	protected void setUp() {
		super.setUp();
	}

	@BeforeAll
	public void setUpLoggedInAccountAndMockJWTHeaders() {
		loggedInAdmin = new Account();
		AccountRole adminRole = new AccountRole();
		adminRole.setRoleId(2);
		adminRole.setRoleType("ROLE_ADMIN");
		loggedInAdmin.setAccountNumber(2);
		loggedInAdmin.setUsername("admin");

		loggedInUser = new Account();
		AccountRole userRole = new AccountRole();
		userRole.setRoleId(1);
		userRole.setRoleType("ROLE_USER");
		loggedInUser.setAccountNumber(1);
		loggedInUser.setUsername("userTest332");

		jwtMockHeaders = new HttpHeaders();
		jwtMockHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		jwtMockHeaders.set("Authorization", "Bearer fauxvaluehere");

	}

	@Test
	void getAccountById_LoggedInAdmin_GetExistingAccount() throws Exception {
		String uri = "/accounts/-1";

		Account expectedAccount = new Account();
		expectedAccount.setAccountNumber(-1);
		expectedAccount.setUsername("defaultusername");

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();
		int status = mvcResult.getResponse().getStatus();
		Account returnedAccount = mapFromJson(mvcResult.getResponse().getContentAsString(), Account.class);

		assertEquals(200, status);
		assertEquals(expectedAccount.getUsername(), returnedAccount.getUsername());
	}

	@Test
	void getAccountById_LoggedInAdmin_GetNonExistantAccount() throws Exception {
		String uri = "/accounts/3";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();
		int status = mvcResult.getResponse().getStatus();

		assertEquals(404, status);

	}

	@Test
	void getAccountById_LoggedInUser_GetAnotherAccount() throws Exception {
		String uri = "/accounts/-1";

		Account expectedAccount = new Account();
		expectedAccount.setAccountNumber(1);
		expectedAccount.setUsername("defaultusername");

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInUser), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();
		int status = mvcResult.getResponse().getStatus();

		assertEquals(401, status);
	}

	@Test
	void getAccountById_LoggedInUser_GetOwnAccount() throws Exception {
		String uri = "/accounts/1";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInUser), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders)).andReturn();
		int status = mvcResult.getResponse().getStatus();
		Account returnedAccount = mapFromJson(mvcResult.getResponse().getContentAsString(), Account.class);

		assertEquals(200, status);
		assertEquals(loggedInUser.getUsername(), returnedAccount.getUsername());
	}

	@Test
	void getAccountById_unAuthenticatedAccount() throws Exception {
		String uri = "/accounts/3";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		HttpHeaders unauthenticatedHeaders = new HttpHeaders();
		unauthenticatedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		unauthenticatedHeaders.set("Authorization", "Should be rejected");

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();
		int status = mvcResult.getResponse().getStatus();

		assertEquals(404, status);
	}

	@Test
	void getUserAccountsWithFilter_LoggedInAdmin_NoNameFilter() throws Exception {
		String uri = "/accounts/users";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		HttpHeaders unauthenticatedHeaders = new HttpHeaders();
		unauthenticatedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		unauthenticatedHeaders.set("Authorization", "Bearer Should Work");

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
	}

	@Test
	void getUserAccountsWithFilter_LoggedInAdmin_WithNameFilter() throws Exception {
		String uri = "/accounts/users/?nameFilter=user";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		HttpHeaders unauthenticatedHeaders = new HttpHeaders();
		unauthenticatedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		unauthenticatedHeaders.set("Authorization", "Bearer Should Work");

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
	}

	@Test
	void getUserAccountsWithFilter_LoggedInUser_ShouldNotBeAuthorized() throws Exception {
		String uri = "/accounts/users";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInUser), MediaType.APPLICATION_JSON));

		HttpHeaders unauthenticatedHeaders = new HttpHeaders();
		unauthenticatedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		unauthenticatedHeaders.set("Authorization", "Bearer Should Work");

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();
		assertEquals(401, status);
	}

	@Test
	void getUserAccountsWithFilter_LoggedInAdmin_NoAccountsFoundAfterFilter() throws Exception {

		String uri = "/accounts/users/?nameFilter=adlkahbsdnfgjlamhsbdkjlacxsb";// Assumed to never be a username

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		HttpHeaders unauthenticatedHeaders = new HttpHeaders();
		unauthenticatedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		unauthenticatedHeaders.set("Authorization", "Bearer Should Work");

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();
		assertEquals(404, status);
	}

	@Test
	void getAdminAccountsWithFilter_LoggedInAdmin_NoNameFilter() throws Exception {
		String uri = "/accounts/admins";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		HttpHeaders unauthenticatedHeaders = new HttpHeaders();
		unauthenticatedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		unauthenticatedHeaders.set("Authorization", "Bearer Should Work");

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
	}

	@Test
	void getAdminAccountsWithFilter_LoggedInAdmin_WithNameFilter() throws Exception {
		String uri = "/accounts/admins/?nameFilter=admin";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		HttpHeaders unauthenticatedHeaders = new HttpHeaders();
		unauthenticatedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		unauthenticatedHeaders.set("Authorization", "Bearer Should Work");

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
	}

	@Test
	void getAdminAccountsWithFilter_LoggedInUser_ShouldNotBeAuthorized() throws Exception {
		String uri = "/accounts/admins";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInUser), MediaType.APPLICATION_JSON));

		HttpHeaders unauthenticatedHeaders = new HttpHeaders();
		unauthenticatedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		unauthenticatedHeaders.set("Authorization", "Bearer Should Work");

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();
		assertEquals(401, status);
	}

	@Test
	void getAdminAccountsWithFilter_LoggedInAdmin_NoAccountsFoundAfterFilter() throws Exception {
		String uri = "/accounts/admins/?nameFilter=adlkahbsdnfgjlamhsbdkjlacxsb";// Assumed to never be a username

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		HttpHeaders unauthenticatedHeaders = new HttpHeaders();
		unauthenticatedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		unauthenticatedHeaders.set("Authorization", "Bearer Should Work");

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();
		assertEquals(404, status);
	}

	@Transactional
	@Test
	void updateAccount_LoggedInAdmin_SuccessfulUpdate() throws Exception {
		String uri = "/accounts/-1";

		Account accountToUpdate = new Account();
		AccountRole adminAccountRole = new AccountRole();
		adminAccountRole.setRoleId(2);
		adminAccountRole.setRoleType("ROLE_ADMIN");
		accountToUpdate.setRole(adminAccountRole);
		accountToUpdate.setAccountNumber(1);
		accountToUpdate.setUsername("updatedName");
		accountToUpdate.setEmail("updated@example.com");
		accountToUpdate.setDateCreated(LocalDate.now());
		accountToUpdate.setPassword("password");

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc
				.perform(MockMvcRequestBuilders.put(uri).headers(jwtMockHeaders).contentType(MediaType.APPLICATION_JSON)
						.content(mapToJson(accountToUpdate)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();
		String responseContent = mvcResult.getResponse().getContentAsString();
		Account returnedAccount = super.mapFromJson(responseContent, Account.class);

		assertEquals(200, status);
		assertEquals(returnedAccount.getUsername(), accountToUpdate.getUsername());
		assertEquals(returnedAccount.getEmail(), accountToUpdate.getEmail());
	}

	@Transactional
	@Test
	void updateAccount_LoggedInAdmin_DuplicateName() throws Exception {
		String uri = "/accounts/-1";

		Account accountToUpdate = new Account();
		AccountRole adminAccountRole = new AccountRole();
		adminAccountRole.setRoleId(2);
		adminAccountRole.setRoleType("ROLE_ADMIN");
		accountToUpdate.setRole(adminAccountRole);
		accountToUpdate.setAccountNumber(1);
		accountToUpdate.setUsername("userTest332");// duplicate username
		accountToUpdate.setEmail("updated@example.com");

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc
				.perform(MockMvcRequestBuilders.put(uri).headers(jwtMockHeaders).contentType(MediaType.APPLICATION_JSON)
						.content(mapToJson(accountToUpdate)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();

		assertEquals(409, status);
	}

	@Transactional
	@Test
	void updateAccount_LoggedInAdmin_UsernameIsTooLong() throws Exception {
		String uri = "/accounts/-1";

		Account accountToUpdate = new Account();
		AccountRole adminAccountRole = new AccountRole();
		adminAccountRole.setRoleId(2);
		adminAccountRole.setRoleType("ROLE_ADMIN");
		accountToUpdate.setRole(adminAccountRole);
		accountToUpdate.setAccountNumber(1);
		accountToUpdate.setUsername("ThisNameIsTooLongToBeUsedInOurDatabaseCashMoneyTesting");// More than 45 characters
		accountToUpdate.setEmail("updated@example.com");

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc
				.perform(MockMvcRequestBuilders.put(uri).headers(jwtMockHeaders).contentType(MediaType.APPLICATION_JSON)
						.content(mapToJson(accountToUpdate)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();

		assertEquals(413, status);
	}

	@Transactional
	@Test
	void updateAccount_LoggedInAdmin_EmailIsTooLong() throws Exception {
		String uri = "/accounts/-1";

		Account accountToUpdate = new Account();
		AccountRole adminAccountRole = new AccountRole();
		adminAccountRole.setRoleId(2);
		adminAccountRole.setRoleType("ROLE_ADMIN");
		accountToUpdate.setRole(adminAccountRole);
		accountToUpdate.setAccountNumber(1);
		accountToUpdate.setUsername("defaultuserUpdate");
		accountToUpdate.setEmail("ThisNameIsTooLongToBeUsedInOurDatabaseCashMoneyTesting");// More than 45 characters

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc
				.perform(MockMvcRequestBuilders.put(uri).headers(jwtMockHeaders).contentType(MediaType.APPLICATION_JSON)
						.content(mapToJson(accountToUpdate)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();

		assertEquals(413, status);
	}

	@Transactional
	@Test
	void updateAccount_LoggedInUser_SuccessfulUpdate() throws Exception {
		String uri = "/accounts/1";

		Account accountToUpdate = new Account();
		AccountRole userAccountRole = new AccountRole();
		userAccountRole.setRoleId(1);
		userAccountRole.setRoleType("ROLE_USER");
		accountToUpdate.setRole(userAccountRole);
		accountToUpdate.setAccountNumber(1);
		accountToUpdate.setUsername("updatedName");
		accountToUpdate.setEmail("updated@example.com");

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInUser), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc
				.perform(MockMvcRequestBuilders.put(uri).headers(jwtMockHeaders).contentType(MediaType.APPLICATION_JSON)
						.content(mapToJson(accountToUpdate)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();
		String responseContent = mvcResult.getResponse().getContentAsString();
		Account returnedAccount = super.mapFromJson(responseContent, Account.class);

		assertEquals(200, status);
		assertEquals(returnedAccount.getUsername(), accountToUpdate.getUsername());
		assertEquals(returnedAccount.getEmail(), accountToUpdate.getEmail());
	}

	@Transactional
	@Test
	void updateAccount_LoggedInUser_AttemptingToEditAnotherAccount() throws Exception {
		String uri = "/accounts/-1";

		Account accountToUpdate = new Account();
		AccountRole userAccountRole = new AccountRole();
		userAccountRole.setRoleId(1);
		userAccountRole.setRoleType("ROLE_USER");
		accountToUpdate.setRole(userAccountRole);
		accountToUpdate.setAccountNumber(-1);
		accountToUpdate.setUsername("updatedName");
		accountToUpdate.setEmail("updated@example.com");

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInUser), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc
				.perform(MockMvcRequestBuilders.put(uri).headers(jwtMockHeaders).contentType(MediaType.APPLICATION_JSON)
						.content(mapToJson(accountToUpdate)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		int status = mvcResult.getResponse().getStatus();

		assertEquals(401, status);
	}

	@Transactional
	@Test
	void deactivateAccount_LoggedInAdmin_DeletingExistingAccount() throws Exception {
		String uri = "/accounts/1";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInAdmin), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.delete(uri).headers(jwtMockHeaders).contentType(MediaType.APPLICATION_JSON)
						.content(mapToJson(loggedInUser)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();
		int status = mvcResult.getResponse().getStatus();

		assertEquals(200, status);
	}

	@Transactional
	@Test
	void deactivateAccount_LoggedInUser_DeactivatingAnotherAccount() throws Exception {
		String uri = "/accounts/-1";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInUser), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.delete(uri).headers(jwtMockHeaders).contentType(MediaType.APPLICATION_JSON)
						.content(mapToJson(loggedInUser)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();
		int status = mvcResult.getResponse().getStatus();

		assertEquals(401, status);
	}

	@Transactional
	@Test
	void deactivateAccount_LoggedInUser_DeactivatingOwnAccount() throws Exception {
		String uri = "/accounts/1";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount"))
				.andRespond(withSuccess(mapToJson(loggedInUser), MediaType.APPLICATION_JSON));

		MvcResult mvcResult = mvc.perform(
				MockMvcRequestBuilders.delete(uri).headers(jwtMockHeaders).contentType(MediaType.APPLICATION_JSON)
						.content(mapToJson(loggedInUser)).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();
		int status = mvcResult.getResponse().getStatus();

		assertEquals(200, status);

	}

}
