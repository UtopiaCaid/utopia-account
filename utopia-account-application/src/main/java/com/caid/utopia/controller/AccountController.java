package com.caid.utopia.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.caid.utopia.entity.Traveler;
import com.caid.utopia.service.AccountService;
import com.caid.utopia.service.TravelerService;

//@SpringBootApplication(scanBasePackages = "com.caid.utopia")
@CrossOrigin(origins = "${message.origin}")
@RequestMapping("/accounts")
@RestController
public class AccountController {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	AccountService accountService;

	@Autowired
	TravelerService travelerService;

	@GetMapping(path = "/{accountNumber}", produces = "application/json")
	public ResponseEntity<Account> getAccountsById(@PathVariable Integer accountNumber,
			@RequestHeader(name = "Authorization") String token) {
		Account account = accountService.getAccountById(accountNumber, token);
		return new ResponseEntity<Account>(account, HttpStatus.OK);
	}

	@GetMapping(path = "/users", produces = "application/json")
	public ResponseEntity<List<Account>> getUserAccountsWithFilter(
			@RequestParam(required = false) Optional<String> nameFilter,
			@RequestHeader(name = "Authorization") String token) {
		List<Account> accounts = accountService.getUserAccounts(nameFilter, token);
		logger.info("User Accounts have been found");
		return new ResponseEntity<List<Account>>(accounts, HttpStatus.OK);
	}

	@GetMapping(path = "/admins", produces = "application/json")
	public ResponseEntity<List<Account>> getAdminAccountsWithFilter(
			@RequestParam(required = false) Optional<String> nameFilter,
			@RequestHeader(name = "Authorization") String token) {
		List<Account> accounts = accountService.getAdminAccounts(nameFilter, token);
		logger.info("User Account have been found");
		return new ResponseEntity<List<Account>>(accounts, HttpStatus.OK);
	}

	@RequestMapping(path = "/{accountNumber}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Account> updateAccount(@RequestBody Account account, @PathVariable Integer accountNumber,
			@RequestHeader(name = "Authorization") String token) {
		account.setAccountNumber(accountNumber);
		account = accountService.saveAccount(account, token);
		logger.info("Successfully Updated Account");
		return new ResponseEntity<Account>(account, HttpStatus.OK);
	}

	@RequestMapping(path = "/{accountNumber}", method = RequestMethod.DELETE, consumes = "application/json") //
	public ResponseEntity<Object> deactivateAccount(@RequestBody Account account, @PathVariable Integer accountNumber,
			@RequestHeader(name = "Authorization") String jwtToken) {
		account.setAccountNumber(accountNumber);
		accountService.deactivateAccount(account, jwtToken);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(path = "/{accountNumber}/travelers")
	public ResponseEntity<List<Traveler>> getTravelersForAccount(@PathVariable Integer accountNumber,
			@RequestHeader(name = "Authorization") String jwtToken) {
		List<Traveler> travelers = travelerService.getTravelersForAccount(accountNumber, jwtToken);
		return new ResponseEntity<List<Traveler>>(travelers, HttpStatus.OK);
	}

	@RequestMapping(path = "/{accountNumber}/travelers", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<Traveler> postNewTraveler(@PathVariable Integer accountNumber, @RequestBody Traveler traveler,
			@RequestHeader(name = "Authorization") String jwtToken) {
		traveler = travelerService.saveTraveler(accountNumber, traveler, jwtToken);
		return new ResponseEntity<Traveler>(traveler, HttpStatus.CREATED);
	}

	@RequestMapping(path = "/{accountNumber}/travelers/{travelerId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Traveler> updateTravelerForAccount(@PathVariable Integer accountNumber,
			@PathVariable Integer travelerId, @RequestBody Traveler traveler,
			@RequestHeader(name = "Authorization") String jwtToken) {
		traveler.setTravelerId(travelerId);
		traveler = travelerService.saveTraveler(accountNumber, traveler, jwtToken);
		return new ResponseEntity<Traveler>(traveler, HttpStatus.OK);
	}

	@RequestMapping(path = "/{accountNumber/travelers/{travelerId}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> deleteTravelerForAccount(@PathVariable Integer accountNumber,
			@PathVariable Integer travelerId, @RequestBody Traveler traveler,
			@RequestHeader(name = "Authorization") String jwtToken) {
		traveler.setTravelerId(travelerId);
		travelerService.deleteTraveler(accountNumber, traveler, jwtToken);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
