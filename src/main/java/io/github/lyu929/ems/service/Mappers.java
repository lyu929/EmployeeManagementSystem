package io.github.lyu929.ems.service;

import io.github.lyu929.ems.domain.AuditEntry;
import io.github.lyu929.ems.domain.Division;
import io.github.lyu929.ems.domain.Employee;
import io.github.lyu929.ems.domain.JobTitle;
import io.github.lyu929.ems.domain.PayStatement;
import io.github.lyu929.ems.domain.UserAccount;
import io.github.lyu929.ems.security.SsnProtector;
import io.github.lyu929.ems.web.dto.AuditResponse;
import io.github.lyu929.ems.web.dto.EmployeeResponse;
import io.github.lyu929.ems.web.dto.PayStatementResponse;
import io.github.lyu929.ems.web.dto.Ref;
import io.github.lyu929.ems.web.dto.UserResponse;

/** Entity -> DTO mapping. Call inside a transaction (associations are lazy). */
public final class Mappers {

    private Mappers() {}

    public static EmployeeResponse employee(Employee e) {
        return new EmployeeResponse(e.getId(), e.getFirstName(), e.getLastName(), e.getEmail(), e.getHireDate(),
                e.getSalary(), SsnProtector.mask(e.getSsnLast4()), division(e.getDivision()), jobTitle(e.getJobTitle()),
                e.getVersion());
    }

    public static Ref division(Division d) {
        return d == null ? null : new Ref(d.getId(), d.getName());
    }

    public static Ref jobTitle(JobTitle j) {
        return j == null ? null : new Ref(j.getId(), j.getTitle());
    }

    public static PayStatementResponse payStatement(PayStatement p) {
        return new PayStatementResponse(p.getId(), p.getEmployee().getId(), p.getPayDate(), p.getEarnings(),
                p.getFedTax(), p.getFedMed(), p.getFedSs(), p.getStateTax(), p.getRetire401k(), p.getHealthCare(),
                p.totalDeductions(), p.netPay());
    }

    public static UserResponse user(UserAccount u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getRole(),
                u.getEmployee() == null ? null : u.getEmployee().getId(), u.isEnabled());
    }

    public static AuditResponse audit(AuditEntry a) {
        return new AuditResponse(a.getId(), a.getOccurredAt(), a.getActor(), a.getAction(), a.getEntityType(),
                a.getEntityId(), a.getDetails());
    }
}
