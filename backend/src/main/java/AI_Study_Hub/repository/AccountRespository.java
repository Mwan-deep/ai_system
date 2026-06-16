package AI_Study_Hub.repository;

import AI_Study_Hub.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRespository extends JpaRepository<Account, Long> {
    Boolean existsAccountsByUserName(String userName);
    Boolean existsAccountsByEmail(String email);
    Optional<Account> findAccountByUserName(String userName);
    Optional<Account> findAccountByEmail(String email);
    Account findAccountById(Long id);
}
