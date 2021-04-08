package com.caid.utopia.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.caid.utopia.entity.Traveler;

@Repository
public interface TravelerRepo extends JpaRepository<Traveler, Integer> {

	@Query(" FROM Traveler t WHERE t.account.accountNumber = :accountNo")
	List<Traveler> getTravelersAssociatedWithAccount(@Param("accountNo") Integer accountNumber);

}
