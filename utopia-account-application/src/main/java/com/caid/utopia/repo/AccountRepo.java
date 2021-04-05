package com.caid.utopia.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.caid.utopia.entity.Account;

@Repository
public interface AccountRepo extends JpaRepository<Account, Integer>{

	@Query(" FROM Account a WHERE a.username=:username")
	Optional<Account> findByUsername(String username);
	
	@Query(" FROM Account a WHERE a.role=1")
	List<Account> getAllUsers();
	
	@Query(" FROM Account a WHERE username LIKE %:name% AND a.role='1'")
	List<Account> getAllUsersWithFilter(@Param("name") String filterString);
	
	@Query(" FROM Account a WHERE a.role=2")
	List<Account> getAllAdmins();
	
	@Query(" FROM Account a WHERE username LIKE %:name% AND a.role='2'")
	List<Account> getAllAdminsWithFilter(@Param("name") String filterString);

	@Query("SELECT case WHEN COUNT(a)>0 THEN true ELSE false END FROM Account a WHERE lower(a.username) like lower(:usernameToCheck) AND a.accountNumber <> :idToIgnore")	
	boolean checkIfUsernameIsUsedBySomeoneElse(@Param("usernameToCheck") String userName, @Param("idToIgnore") Integer id);

	@Modifying
	@Query("UPDATE Account a SET a.username=null, a.email=null, a.password=null WHERE a.accountNumber=:accountNumber")
	void deactivateLoginForAccount(@Param("accountNumber")Integer accountNumber);

	
}
	
