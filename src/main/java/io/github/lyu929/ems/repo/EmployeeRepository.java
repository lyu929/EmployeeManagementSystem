package io.github.lyu929.ems.repo;

import io.github.lyu929.ems.domain.Employee;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @Query("select e from Employee e left join fetch e.division left join fetch e.jobTitle where e.id = :id")
    Optional<Employee> findDetailedById(@Param("id") Integer id);

    /** Case-insensitive substring search over names and e-mail; {@code q} must be escaped with {@code !}. */
    @Query(value = """
            select e from Employee e left join fetch e.division left join fetch e.jobTitle
            where lower(e.firstName) like lower(concat('%', :q, '%')) escape '!'
               or lower(e.lastName) like lower(concat('%', :q, '%')) escape '!'
               or lower(e.email) like lower(concat('%', :q, '%')) escape '!'
            """,
            countQuery = """
            select count(e) from Employee e
            where lower(e.firstName) like lower(concat('%', :q, '%')) escape '!'
               or lower(e.lastName) like lower(concat('%', :q, '%')) escape '!'
               or lower(e.email) like lower(concat('%', :q, '%')) escape '!'
            """)
    Page<Employee> search(@Param("q") String q, Pageable pageable);

    @Query(value = "select e from Employee e left join fetch e.division left join fetch e.jobTitle",
            countQuery = "select count(e) from Employee e")
    Page<Employee> findAllDetailed(Pageable pageable);

    @Query("select e from Employee e left join fetch e.division left join fetch e.jobTitle where e.ssnHash = :hash")
    Optional<Employee> findBySsnHash(@Param("hash") String hash);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    boolean existsBySsnHash(String ssnHash);

    boolean existsBySsnHashAndIdNot(String ssnHash, Integer id);

    /** Candidates for a salary adjustment; a null filter means "no restriction". */
    @Query("""
            select e from Employee e
            where (:min is null or e.salary >= :min)
              and (:max is null or e.salary <= :max)
              and (:divisionId is null or e.division.id = :divisionId)
            order by e.id
            """)
    List<Employee> findForAdjustment(@Param("min") BigDecimal min, @Param("max") BigDecimal max,
            @Param("divisionId") Integer divisionId);

    @Query("""
            select e from Employee e left join fetch e.jobTitle
            where e.hireDate between :from and :to
            order by e.hireDate desc, e.id
            """)
    List<Employee> findHiredBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
