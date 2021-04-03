package com.caid.utopia.controller;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.springframework.web.client.RestTemplate;

import com.caid.utopia.UtopiaAccountApplicationTests;
import com.caid.utopia.entity.AccountRole;
import com.caid.utopia.entity.Account;

@TestInstance(value = Lifecycle.PER_CLASS)
public class AccountControllerTests extends UtopiaAccountApplicationTests{

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
	void getAccountById_LoggedInAdmin_GetNonExistantAccount() throws Exception {
		String uri = "/accounts/3";
		
		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount")).andRespond(withSuccess(mapToJson(loggedInAdmin),MediaType.APPLICATION_JSON));
		
		
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();
		int status = mvcResult.getResponse().getStatus();
		
		assertEquals(404, status);
		
	}
	
	@Test
	void getAccountById_LoggedInAdmin_GetExistingAccount() throws Exception {
		String uri = "/accounts/-1";
		
		Account expectedAccount = new Account();
		expectedAccount.setAccountNumber(-1);
		expectedAccount.setUsername("defaultusername");

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount")).andRespond(withSuccess(mapToJson(loggedInAdmin),MediaType.APPLICATION_JSON));
		
		
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();
		int status = mvcResult.getResponse().getStatus();
		Account returnedAccount = mapFromJson(mvcResult.getResponse().getContentAsString(), Account.class);
		
		assertEquals(200, status);
		assertEquals(expectedAccount.getUsername(), returnedAccount.getUsername());
	}
	
	@Test
	void getAccountById_LoggedInUser_GetAnotherAccount() throws Exception {
		String uri = "/accounts/-1";
		
		Account expectedAccount = new Account();
		expectedAccount.setAccountNumber(1);
		expectedAccount.setUsername("defaultusername");

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount")).andRespond(withSuccess(mapToJson(loggedInUser),MediaType.APPLICATION_JSON));
		
		
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();
		int status = mvcResult.getResponse().getStatus();
		
		assertEquals(401, status);
	}
	
	@Test
	void getAccountById_LoggedInUser_GetOwnAccount() throws Exception {
		String uri = "/accounts/1";

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount")).andRespond(withSuccess(mapToJson(loggedInUser),MediaType.APPLICATION_JSON));
		
		
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();
		int status = mvcResult.getResponse().getStatus();
		Account returnedAccount = mapFromJson(mvcResult.getResponse().getContentAsString(), Account.class);
		
		assertEquals(200, status);
		assertEquals(loggedInUser.getUsername(), returnedAccount.getUsername());
	}
	
	@Test
	void getAccountById_unAuthenticatedAccount() throws Exception {
		String uri = "/accounts/3";
		
		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockServer.expect(requestTo("http://utopiaauthentication/getSecurityAccount")).andRespond(withSuccess(mapToJson(loggedInAdmin),MediaType.APPLICATION_JSON));
		
		HttpHeaders unauthenticatedHeaders = new HttpHeaders();
		unauthenticatedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		unauthenticatedHeaders.set("Authorization", "Should be rejected");
		
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).headers(jwtMockHeaders).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();
		int status = mvcResult.getResponse().getStatus();
		
		assertEquals(404, status);
		
	}
	
}
