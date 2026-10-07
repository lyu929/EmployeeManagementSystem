package io.github.lyu929.ems.repo;

import io.github.lyu929.ems.domain.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    @Query("select u from UserAccount u left join fetch u.employee where lower(u.username) = lower(:username)")
    Optional<UserAccount> findByUsername(@Param("username") String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmployeeId(Integer employeeId);
}
