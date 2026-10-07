package io.github.lyu929.ems.service;

import io.github.lyu929.ems.error.InvalidRequestException;
import io.github.lyu929.ems.repo.EmployeeRepository;
import io.github.lyu929.ems.repo.GroupTotal;
import io.github.lyu929.ems.repo.PayStatementRepository;
import io.github.lyu929.ems.repo.PayrollTotals;
import io.github.lyu929.ems.web.dto.GroupTotalResponse;
import io.github.lyu929.ems.web.dto.NewHireResponse;
import io.github.lyu929.ems.web.dto.PayrollSummaryResponse;
import io.github.lyu929.ems.web.dto.PeriodReportResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The HR reports of the course project, as aggregate queries in the database. */
@Service
@Transactional(readOnly = true)
public class ReportService {

    static final String UNASSIGNED = "(unassigned)";

    private final PayStatementRepository statements;
    private final EmployeeRepository employees;

    public ReportService(PayStatementRepository statements, EmployeeRepository employees) {
        this.statements = statements;
        this.employees = employees;
    }

    public PayrollSummaryResponse payrollSummary(LocalDate payDate) {
        PayrollTotals t = statements.totalsFor(payDate)
                .orElse(new PayrollTotals(payDate, 0L, BigDecimal.ZERO, BigDecimal.ZERO));
        BigDecimal earnings = Money.round(t.earnings());
        BigDecimal deductions = Money.round(t.deductions());
        return new PayrollSummaryResponse(payDate, t.statements(), earnings, deductions, earnings.subtract(deductions));
    }

    public PeriodReportResponse payByJobTitle(YearMonth month) {
        return period(month, "jobTitle", statements.totalsByJobTitle(month.atDay(1), month.atEndOfMonth()));
    }

    public PeriodReportResponse payByDivision(YearMonth month) {
        return period(month, "division", statements.totalsByDivision(month.atDay(1), month.atEndOfMonth()));
    }

    public List<NewHireResponse> newHires(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new InvalidRequestException("'from' must not be after 'to'");
        }
        return employees.findHiredBetween(from, to).stream()
                .map(e -> new NewHireResponse(e.getId(), e.getFirstName(), e.getLastName(), e.getHireDate(),
                        e.getJobTitle() == null ? null : e.getJobTitle().getTitle()))
                .toList();
    }

    private static PeriodReportResponse period(YearMonth month, String groupedBy, List<GroupTotal> totals) {
        List<GroupTotalResponse> rows = totals.stream()
                .map(g -> new GroupTotalResponse(g.groupName() == null ? UNASSIGNED : g.groupName(), g.statements(),
                        Money.round(g.earnings())))
                .toList();
        BigDecimal grand = rows.stream().map(GroupTotalResponse::totalPay).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PeriodReportResponse(month.toString(), groupedBy, rows, Money.round(grand));
    }
}
