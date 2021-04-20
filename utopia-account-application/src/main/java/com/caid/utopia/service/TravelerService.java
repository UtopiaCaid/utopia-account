package com.caid.utopia.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.caid.utopia.entity.Account;
import com.caid.utopia.entity.Traveler;
import com.caid.utopia.exception.AccountNotFoundException;
import com.caid.utopia.exception.AuthorizationException;
import com.caid.utopia.repo.AccountRepo;
import com.caid.utopia.repo.TravelerRepo;

@Service
public class TravelerService {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	AccountRepo accountRepo;

	@Autowired
	TravelerRepo travelerRepo;

	@Autowired
	RestTemplate restTemplate;

	public List<Traveler> getTravelersForAccount(Integer accountNumber, String jwtToken) {
		// TODO Auto-generated method stub
		Account loggedInAccount = getLoggedInAccountByJWT(jwtToken);
		ensureIfAccountIsAdminOrExpectedUser(loggedInAccount, accountNumber);
		return travelerRepo.getTravelersAssociatedWithAccount(accountNumber);
	}

	public Traveler saveTraveler(Integer accountNumber, Traveler travelerToAdd, String jwtToken) {
		// TODO Auto-generated method stub
		Account loggedInAccount = getLoggedInAccountByJWT(jwtToken);
		ensureIfAccountIsAdminOrExpectedUser(loggedInAccount, accountNumber);
		Optional<Account> optAccount = accountRepo.findById(accountNumber);
		Account accountToAddTravelerTo = null;
		if (optAccount.isPresent()) {
			accountToAddTravelerTo = optAccount.get();
		} else {
			throw new AccountNotFoundException();
		}
		travelerToAdd.setAccount(accountToAddTravelerTo);

		return travelerRepo.save(travelerToAdd);
	}

	public void deleteTraveler(Integer accountNumber, Traveler traveler, String jwtToken) {
		// TODO Auto-generated method stub
		Account loggedInAccount = getLoggedInAccountByJWT(jwtToken);
		ensureIfAccountIsAdminOrExpectedUser(loggedInAccount, accountNumber);
		travelerRepo.delete(traveler);
	}

	public void ensureIfAccountIsAdminOrExpectedUser(Account loggedInAccount, Integer accountNumber) {
		if ("ROLE_ADMIN".equals(loggedInAccount.getRole().getRoleType())
				|| accountNumber == loggedInAccount.getAccountNumber()) {
			logger.info("AccountService.GetAccountById: Logged in account has been located user: {}",
					loggedInAccount.getUsername());
		} else {
			throw new AuthorizationException();
		}
	}

	public Account getAccountByUsername(String username) {
		Optional<Account> account = accountRepo.findByUsername(username);
		if (account.isPresent()) {
			return account.get();
		} else {
			throw new AccountNotFoundException();
		}
	}

	public Account getLoggedInAccountByJWT(String jwtToken) {
		logger.info("getLoggedInAccountByJWT: Checking if loggedIn Account has correct permissions.");
		logger.info("getLoggedInAccountByJWT: Token Value: {}", jwtToken);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", jwtToken);
		if (!jwtToken.contains("Bearer"))
			throw new AuthorizationException();
		HttpEntity<Account> entity = new HttpEntity<Account>(headers);
		ResponseEntity<Account> accountResponseEntity = restTemplate
				.exchange("http://${message.utopia.alb.dns}/authentication", HttpMethod.GET, entity, Account.class);
//		Check for role then return account if role is admin or is user and the id matched with that of the jwtToken
		Account loggedInAccount = accountResponseEntity.getBody();
		logger.info("getLoggedInAccountByJWT: Account making the request: {}, ", loggedInAccount.getUsername());
		return getAccountByUsername(loggedInAccount.getUsername());
	}

}
