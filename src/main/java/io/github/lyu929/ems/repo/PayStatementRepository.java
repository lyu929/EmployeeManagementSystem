package io.github.lyu929.ems.repo;

import io.github.lyu929.ems.domain.PayStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PayStatementRepository extends JpaRepository<PayStatement, Long> {

    @Query("select p from PayStatement p where p.employee.id = :employeeId order by p.payDate desc")
    List<PayStatement> findForEmployee(@Param("employeeId") Integer employeeId);

    boolean existsByEmployeeIdAndPayDate(Integer employeeId, LocalDate payDate);

    @Query("""
            select new io.github.lyu929.ems.repo.PayrollTotals(p.payDate, count(p), sum(p.earnings),
                   sum(p.fedTax + p.fedMed + p.fedSs + p.stateTax + p.retire401k + p.healthCare))
            from PayStatement p
            where p.payDate = :payDate
            group by p.payDate
            """)
    Optional<PayrollTotals> totalsFor(@Param("payDate") LocalDate payDate);

    @Query("""
            select new io.github.lyu929.ems.repo.GroupTotal(j.title, count(p), sum(p.earnings))
            from PayStatement p join p.employee e left join e.jobTitle j
            where p.payDate between :from and :to
            group by j.title
            order by j.title
            """)
    List<GroupTotal> totalsByJobTitle(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select new io.github.lyu929.ems.repo.GroupTotal(d.name, count(p), sum(p.earnings))
            from PayStatement p join p.employee e left join e.division d
            where p.payDate between :from and :to
            group by d.name
            order by d.name
            """)
    List<GroupTotal> totalsByDivision(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
