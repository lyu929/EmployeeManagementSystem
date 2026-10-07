package io.github.lyu929.ems.service;

import io.github.lyu929.ems.domain.Division;
import io.github.lyu929.ems.domain.Employee;
import io.github.lyu929.ems.domain.JobTitle;
import io.github.lyu929.ems.error.ConflictException;
import io.github.lyu929.ems.error.InvalidRequestException;
import io.github.lyu929.ems.error.NotFoundException;
import io.github.lyu929.ems.repo.DivisionRepository;
import io.github.lyu929.ems.repo.EmployeeRepository;
import io.github.lyu929.ems.repo.JobTitleRepository;
import io.github.lyu929.ems.security.SsnProtector;
import io.github.lyu929.ems.web.dto.EmployeeRequest;
import io.github.lyu929.ems.web.dto.EmployeeResponse;
import io.github.lyu929.ems.web.dto.PageResponse;
import io.github.lyu929.ems.web.dto.SsnResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Employee CRUD, search and salary changes. Every change is audited. */
@Service
public class EmployeeService {

    private final EmployeeRepository employees;
    private final DivisionRepository divisions;
    private final JobTitleRepository jobTitles;
    private final SsnProtector ssnProtector;
    private final AuditService audit;

    public EmployeeService(EmployeeRepository employees, DivisionRepository divisions, JobTitleRepository jobTitles,
            SsnProtector ssnProtector, AuditService audit) {
        this.employees = employees;
        this.divisions = divisions;
        this.jobTitles = jobTitles;
        this.ssnProtector = ssnProtector;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> search(String query, Pageable pageable) {
        Page<Employee> page = query == null || query.isBlank()
                ? employees.findAllDetailed(pageable)
                : employees.search(escapeLike(query.trim().toLowerCase()), pageable);
        return PageResponse.of(page, Mappers::employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(Integer id) {
        return Mappers.employee(load(id));
    }

    /** Exact SSN lookup through the blind index; the search itself is audited. */
    @Transactional
    public Optional<EmployeeResponse> findBySsn(String ssn) {
        Optional<Employee> found = employees.findBySsnHash(ssnProtector.blindIndex(ssn));
        audit.record("SSN_SEARCH", AuditService.EMPLOYEE, found.map(Employee::getId).orElse(null),
                found.isPresent() ? "match" : "no match");
        return found.map(Mappers::employee);
    }

    @Transactional
    public SsnResponse revealSsn(Integer id) {
        Employee e = load(id);
        if (e.getSsnCiphertext() == null) {
            throw new NotFoundException("employee " + id + " has no SSN on file");
        }
        audit.record("SSN_VIEWED", AuditService.EMPLOYEE, id, null);
        return new SsnResponse(id, ssnProtector.decrypt(e.getSsnCiphertext()));
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest r) {
        if (employees.existsByEmailIgnoreCase(r.email())) {
            throw new ConflictException("an employee with e-mail " + r.email() + " already exists");
        }
        Employee e = new Employee(r.firstName().trim(), r.lastName().trim(), r.email().trim(), r.hireDate(),
                Money.round(r.salary()));
        if (r.ssn() != null) {
            applySsn(e, r.ssn(), null);
        }
        e.assign(division(r.divisionId()), jobTitle(r.jobTitleId()));
        Employee saved = employees.save(e);
        audit.record("EMPLOYEE_CREATED", AuditService.EMPLOYEE, saved.getId(),
                saved.getFirstName() + " " + saved.getLastName() + ", salary " + saved.getSalary());
        return Mappers.employee(saved);
    }

    @Transactional
    public EmployeeResponse update(Integer id, EmployeeRequest r) {
        Employee e = load(id);
        if (employees.existsByEmailIgnoreCaseAndIdNot(r.email(), id)) {
            throw new ConflictException("another employee already uses e-mail " + r.email());
        }
        List<String> changes = new ArrayList<>();
        diff(changes, "firstName", e.getFirstName(), r.firstName().trim());
        diff(changes, "lastName", e.getLastName(), r.lastName().trim());
        diff(changes, "email", e.getEmail(), r.email().trim());
        diff(changes, "hireDate", e.getHireDate(), r.hireDate());
        BigDecimal salary = Money.round(r.salary());
        if (e.getSalary().compareTo(salary) != 0) {
            changes.add("salary " + e.getSalary() + " -> " + salary);
        }
        Division division = division(r.divisionId());
        JobTitle jobTitle = jobTitle(r.jobTitleId());
        diff(changes, "division", idOf(e.getDivision()), r.divisionId());
        diff(changes, "jobTitle", e.getJobTitle() == null ? null : e.getJobTitle().getId(), r.jobTitleId());

        e.rename(r.firstName().trim(), r.lastName().trim());
        e.setEmail(r.email().trim());
        e.setHireDate(r.hireDate());
        e.setSalary(salary);
        e.assign(division, jobTitle);
        if (r.ssn() != null) {
            applySsn(e, r.ssn(), id);
            changes.add("ssn updated");
        }
        audit.record("EMPLOYEE_UPDATED", AuditService.EMPLOYEE, id,
                changes.isEmpty() ? "no changes" : String.join("; ", changes));
        return Mappers.employee(e);
    }

    @Transactional
    public void delete(Integer id) {
        Employee e = load(id);
        String name = e.getFirstName() + " " + e.getLastName();
        employees.delete(e); // pay statements and the login are removed by ON DELETE CASCADE
        employees.flush();
        audit.record("EMPLOYEE_DELETED", AuditService.EMPLOYEE, id, name);
    }

    @Transactional
    public EmployeeResponse setSalary(Integer id, BigDecimal newSalary) {
        Employee e = load(id);
        BigDecimal old = e.getSalary();
        e.setSalary(Money.round(newSalary));
        audit.record("SALARY_CHANGED", AuditService.EMPLOYEE, id, old + " -> " + e.getSalary());
        return Mappers.employee(e);
    }

    Employee load(Integer id) {
        return employees.findDetailedById(id).orElseThrow(() -> new NotFoundException("employee " + id + " not found"));
    }

    private void applySsn(Employee e, String ssn, Integer selfId) {
        String hash = ssnProtector.blindIndex(ssn);
        boolean taken = selfId == null ? employees.existsBySsnHash(hash) : employees.existsBySsnHashAndIdNot(hash, selfId);
        if (taken) {
            throw new ConflictException("another employee already has this SSN");
        }
        e.setSsn(ssnProtector.encrypt(ssn), hash, SsnProtector.last4(ssn));
    }

    private Division division(Integer id) {
        return id == null ? null : divisions.findById(id)
                .orElseThrow(() -> new InvalidRequestException("division " + id + " does not exist"));
    }

    private JobTitle jobTitle(Integer id) {
        return id == null ? null : jobTitles.findById(id)
                .orElseThrow(() -> new InvalidRequestException("job title " + id + " does not exist"));
    }

    private static Integer idOf(Division d) {
        return d == null ? null : d.getId();
    }

    private static void diff(List<String> changes, String field, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            changes.add(field + " " + before + " -> " + after);
        }
    }

    /** Escape LIKE wildcards with '!' (the escape character used by the repository queries). */
    static String escapeLike(String s) {
        return s.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
