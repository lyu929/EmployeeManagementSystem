package io.github.lyu929.ems.web;

import io.github.lyu929.ems.error.NotFoundException;
import io.github.lyu929.ems.service.EmployeeService;
import io.github.lyu929.ems.service.PayrollService;
import io.github.lyu929.ems.web.dto.EmployeeRequest;
import io.github.lyu929.ems.web.dto.EmployeeResponse;
import io.github.lyu929.ems.web.dto.PageResponse;
import io.github.lyu929.ems.web.dto.PayStatementResponse;
import io.github.lyu929.ems.web.dto.SalaryUpdateRequest;
import io.github.lyu929.ems.web.dto.SsnResponse;
import io.github.lyu929.ems.web.dto.SsnSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employees")
@PreAuthorize("hasRole('HR_ADMIN')")
@Validated
@Tag(name = "Employees (HR admin)")
public class EmployeeController {

    private final EmployeeService employees;
    private final PayrollService payroll;

    public EmployeeController(EmployeeService employees, PayrollService payroll) {
        this.employees = employees;
        this.payroll = payroll;
    }

    @GetMapping
    @Operation(summary = "List employees, optionally filtered by a name / e-mail substring")
    public PageResponse<EmployeeResponse> search(@RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size) {
        return employees.search(q, PageRequest.of(page, size, Sort.by("lastName", "firstName", "id")));
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable("id") Integer id) {
        return employees.get(id);
    }

    @PostMapping("/search/ssn")
    @Operation(summary = "Find an employee by exact SSN (sent in the body, never in the URL)")
    public EmployeeResponse findBySsn(@Valid @RequestBody SsnSearchRequest request) {
        return employees.findBySsn(request.ssn())
                .orElseThrow(() -> new NotFoundException("no employee with this SSN"));
    }

    @GetMapping("/{id}/ssn")
    @Operation(summary = "Reveal the full SSN (audited)")
    public SsnResponse revealSsn(@PathVariable("id") Integer id) {
        return employees.revealSsn(id);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = employees.create(request);
        return ResponseEntity.created(URI.create("/api/v1/employees/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable("id") Integer id, @Valid @RequestBody EmployeeRequest request) {
        return employees.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee together with their pay history and login")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        employees.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/salary")
    public EmployeeResponse setSalary(@PathVariable("id") Integer id, @Valid @RequestBody SalaryUpdateRequest request) {
        return employees.setSalary(id, request.salary());
    }

    @GetMapping("/{id}/pay-statements")
    public List<PayStatementResponse> payStatements(@PathVariable("id") Integer id) {
        return payroll.statementsFor(id);
    }
}
