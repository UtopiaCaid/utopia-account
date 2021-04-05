package com.caid.utopia.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.caid.utopia.entity.Account;
import com.caid.utopia.service.AccountService;

//@SpringBootApplication(scanBasePackages = "com.caid.utopia")
@CrossOrigin(origins = "${message.origin}")
@RequestMapping("/accounts")
@RestController
public class AccountController {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	AccountService accountService;

	@GetMapping(path = "/{accountNumber}", produces = "application/json")
	public ResponseEntity<Account> getAccountsById(@PathVariable Integer accountNumber,
			@RequestHeader(name = "Authorization") String token) {
		Account account = accountService.getAccountById(accountNumber, token);
		return new ResponseEntity<Account>(account, HttpStatus.OK);
	}

	@GetMapping(path = "/users", produces = "application/json")
	public ResponseEntity<List<Account>> getUserAccountsWithFilter(
			@RequestParam(required = false) Optional<String> nameFilter) {
		List<Account> accounts = accountService.getUserAccounts(nameFilter);
		logger.info("User Accounts have been found");
		return new ResponseEntity<List<Account>>(accounts, HttpStatus.OK);
	}

	@GetMapping(path = "/admins", produces = "application/json")
	public ResponseEntity<List<Account>> getAdminAccountsWithFilter(
			@RequestParam(required = false) Optional<String> nameFilter) {
		List<Account> accounts = accountService.getAdminAccounts(nameFilter);
		logger.info("User Account have been found");
		return new ResponseEntity<List<Account>>(accounts, HttpStatus.OK);
	}

	// pass the account Number in the path and then add it to the account before
	// passing it to the service. Don't expect is as part of the RequestBody
	@RequestMapping(path = "/{accountNumber}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Account> updateAccount(@RequestBody Account account, @PathVariable Integer accountNumber) {
		account.setAccountNumber(accountNumber);
		account = accountService.saveAccount(account);
		logger.info("Successfully Updated Account");
		return new ResponseEntity<Account>(account, HttpStatus.OK);
	}

	// pass the account Number in the path and then add it to the account before
	// passing it to the service.Don't expect is as part of the RequestBody
	@RequestMapping(path = "/{accountNumber}", method = RequestMethod.DELETE, consumes = "application/json") //
	public ResponseEntity<Object> deleteAccount(@RequestBody Account account, @PathVariable Integer accountNumber) {
		account.setAccountNumber(accountNumber);
		accountService.deactivateAccount(account);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
//	public static void main(String[] args) {
//		SpringApplication.run(AccountController.class, args);
//	}
}
