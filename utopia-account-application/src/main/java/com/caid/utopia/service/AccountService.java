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
import com.caid.utopia.exception.AccountNotFoundException;
import com.caid.utopia.exception.AuthorizationException;
import com.caid.utopia.exception.DuplicateUsernameException;
import com.caid.utopia.exception.OversizedValueException;
import com.caid.utopia.repo.AccountRepo;

@Service
public class AccountService {
	
	@Autowired
	AccountRepo accountRepo;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	RestTemplate restTemplate;
	
	public Account getAccountById(Integer accountNumber, String token){
		logger.info("GetAccountById: Checking if loggedIn Account has correct permissions.");
		logger.info("GetAccountById: Token Value: {}", token);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", token);
		if (!token.contains("Bearer"))
			throw new AuthorizationException();
		HttpEntity<Account> entity = new HttpEntity<Account>(headers);
		ResponseEntity<Account> accountResponseEntity = restTemplate
				.exchange("http://utopiaauthentication/getSecurityAccount", HttpMethod.GET, entity, Account.class);
//		Check for role then return account if role is admin or is user and the id matched with that of the token
		Account loggedInAccount = accountResponseEntity.getBody();
		logger.info("GetAccountById: Account making the request: {}, ", loggedInAccount.getUsername());
		loggedInAccount = getAccountByUsername(loggedInAccount.getUsername());
		if ("ROLE_ADMIN".equals(loggedInAccount.getRole().getRoleType())
				|| accountNumber == loggedInAccount.getAccountNumber()) {		
			logger.info("AccountService.GetAccountById: Logged in account has been located user: {}", loggedInAccount.getUsername());
		} else {
			throw new AuthorizationException();
		}

		return accountRepo.findById(accountNumber).orElseThrow(() -> new AccountNotFoundException());
	}
	
	public Account getAccountByUsername(String username) {
		Optional<Account> account = accountRepo.findByUsername(username);
		if(account.isPresent()) {
			return account.get();
		} else {
			throw new AccountNotFoundException();
		}
	}
	
	//look into why this breaks with no filterString, possibly change the custom repo call when no string is provided
	public List<Account> getUserAccounts(Optional<String> filterString) {
		List<Account> accounts;
		if(filterString.isPresent()) {
			accounts = accountRepo.getAllUsersWithFilter(filterString.get());
		}else {
			accounts = accountRepo.getAllUsers();
		}
		if(accounts.size() == 0) throw new AccountNotFoundException();
		return accounts;
	}
	
	public List<Account> getAdminAccounts(Optional<String> filterString) {
		List<Account> accounts;
		if(filterString.isPresent()) {
			accounts = accountRepo.getAllAdminsWithFilter(filterString.get());
		}else {
			accounts = accountRepo.getAllAdmins();
		}
		if(accounts.size() == 0) throw new AccountNotFoundException();
		return accounts;
	}

	public Account saveAccount(Account account) {
		if(account.getEmail().length() > 45 || account.getUsername().length() > 45) {
			throw new OversizedValueException();
		}
		if(accountRepo.checkIfUsernameIsUsedBySomeoneElse(account.getUsername(), account.getAccountNumber())) {
			throw new DuplicateUsernameException();
		}
		return accountRepo.save(account);
	}

	public void deleteAccount(Account account) {
		accountRepo.findById(account.getAccountNumber()).orElseThrow(() -> new AccountNotFoundException());
		accountRepo.deleteById(account.getAccountNumber());
	}

	public void deactivateAccount(Account account) {
		// TODO Auto-generated method stub
		accountRepo.findById(account.getAccountNumber()).orElseThrow(() -> new AccountNotFoundException());
		accountRepo.deactivateLoginForAccount(account.getAccountNumber());
		
	}
}
