package io.github.lyu929.ems.service;

import io.github.lyu929.ems.domain.Employee;
import io.github.lyu929.ems.error.InvalidRequestException;
import io.github.lyu929.ems.repo.DivisionRepository;
import io.github.lyu929.ems.repo.EmployeeRepository;
import io.github.lyu929.ems.web.dto.SalaryAdjustmentRequest;
import io.github.lyu929.ems.web.dto.SalaryAdjustmentResponse;
import io.github.lyu929.ems.web.dto.SalaryChange;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Percentage raises for a salary range and/or a division. Runs in one transaction: either every
 * matching employee is updated or none is. Each change is audited individually.
 */
@Service
public class SalaryService {

    private final EmployeeRepository employees;
    private final DivisionRepository divisions;
    private final AuditService audit;

    public SalaryService(EmployeeRepository employees, DivisionRepository divisions, AuditService audit) {
        this.employees = employees;
        this.divisions = divisions;
        this.audit = audit;
    }

    @Transactional
    public SalaryAdjustmentResponse adjust(SalaryAdjustmentRequest r) {
        if (r.divisionId() != null && !divisions.existsById(r.divisionId())) {
            throw new InvalidRequestException("division " + r.divisionId() + " does not exist");
        }
        List<Employee> matches = employees.findForAdjustment(r.minSalary(), r.maxSalary(), r.divisionId());
        List<SalaryChange> changes = new ArrayList<>();
        for (Employee e : matches) {
            BigDecimal old = e.getSalary();
            BigDecimal updated = Money.applyPercent(old, r.percent());
            changes.add(new SalaryChange(e.getId(), e.getFirstName() + " " + e.getLastName(), old, updated));
            if (!r.dryRun()) {
                e.setSalary(updated);
                audit.record("SALARY_CHANGED", AuditService.EMPLOYEE, e.getId(),
                        old + " -> " + updated + " (" + r.percent() + "% adjustment)");
            }
        }
        if (!r.dryRun()) {
            audit.record("SALARY_ADJUSTMENT", AuditService.EMPLOYEE, null, String.format(
                    "%s%% for salary %s..%s, division %s: %d employee(s)", r.percent(), r.minSalary(), r.maxSalary(),
                    r.divisionId(), changes.size()));
        }
        return new SalaryAdjustmentResponse(r.percent(), !r.dryRun(), changes.size(), changes);
    }
}
