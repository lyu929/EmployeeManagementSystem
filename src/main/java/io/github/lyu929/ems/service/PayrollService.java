package io.github.lyu929.ems.service;

import io.github.lyu929.ems.domain.Employee;
import io.github.lyu929.ems.error.NotFoundException;
import io.github.lyu929.ems.repo.EmployeeRepository;
import io.github.lyu929.ems.repo.PayStatementRepository;
import io.github.lyu929.ems.web.dto.PayStatementResponse;
import io.github.lyu929.ems.web.dto.PayrollRunResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayrollService {

    private final EmployeeRepository employees;
    private final PayStatementRepository statements;
    private final AuditService audit;

    public PayrollService(EmployeeRepository employees, PayStatementRepository statements, AuditService audit) {
        this.employees = employees;
        this.statements = statements;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<PayStatementResponse> statementsFor(Integer employeeId) {
        if (!employees.existsById(employeeId)) {
            throw new NotFoundException("employee " + employeeId + " not found");
        }
        return statements.findForEmployee(employeeId).stream().map(Mappers::payStatement).toList();
    }

    /** Create one statement per employee for {@code payDate}. Running it twice creates nothing new. */
    @Transactional
    public PayrollRunResponse run(LocalDate payDate) {
        int created = 0;
        int skipped = 0;
        for (Employee e : employees.findAll()) {
            if (statements.existsByEmployeeIdAndPayDate(e.getId(), payDate)) {
                skipped++;
            } else {
                statements.save(PayrollCalculator.statementFor(e, payDate));
                created++;
            }
        }
        audit.record("PAYROLL_RUN", AuditService.PAYROLL, payDate,
                created + " statement(s) created, " + skipped + " already present");
        return new PayrollRunResponse(payDate, created, skipped);
    }
}
