package com.caid.utopia.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caid.utopia.entity.AccountRole;

@Repository
public interface AccountRoleRepo extends JpaRepository<AccountRole, Integer>{

}
