package io.github.lyu929.ems.service;

import io.github.lyu929.ems.domain.Employee;
import io.github.lyu929.ems.domain.UserAccount;
import io.github.lyu929.ems.error.ConflictException;
import io.github.lyu929.ems.error.InvalidRequestException;
import io.github.lyu929.ems.error.NotFoundException;
import io.github.lyu929.ems.repo.EmployeeRepository;
import io.github.lyu929.ems.repo.UserAccountRepository;
import io.github.lyu929.ems.web.dto.CreateUserRequest;
import io.github.lyu929.ems.web.dto.MeResponse;
import io.github.lyu929.ems.web.dto.PasswordChangeRequest;
import io.github.lyu929.ems.web.dto.PayStatementResponse;
import io.github.lyu929.ems.web.dto.UserResponse;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserAccountRepository users;
    private final EmployeeRepository employees;
    private final PasswordEncoder passwordEncoder;
    private final PayrollService payroll;
    private final AuditService audit;

    public UserService(UserAccountRepository users, EmployeeRepository employees, PasswordEncoder passwordEncoder,
            PayrollService payroll, AuditService audit) {
        this.users = users;
        this.employees = employees;
        this.passwordEncoder = passwordEncoder;
        this.payroll = payroll;
        this.audit = audit;
    }

    @Transactional
    public UserResponse create(CreateUserRequest r) {
        if (users.existsByUsernameIgnoreCase(r.username())) {
            throw new ConflictException("username " + r.username() + " is taken");
        }
        Employee employee = null;
        if (r.employeeId() != null) {
            employee = employees.findById(r.employeeId())
                    .orElseThrow(() -> new InvalidRequestException("employee " + r.employeeId() + " does not exist"));
            if (users.existsByEmployeeId(r.employeeId())) {
                throw new ConflictException("employee " + r.employeeId() + " already has a login");
            }
        }
        UserAccount saved = users.save(new UserAccount(r.username(), passwordEncoder.encode(r.password()), r.role(),
                employee));
        audit.record("USER_CREATED", AuditService.USER, saved.getId(), r.username() + " (" + r.role() + ")");
        return Mappers.user(saved);
    }

    @Transactional(readOnly = true)
    public MeResponse me(String username) {
        UserAccount u = current(username);
        return new MeResponse(u.getUsername(), u.getRole().name(),
                u.getEmployee() == null ? null : Mappers.employee(employeeOf(u)));
    }

    /** An employee's own pay history; HR admins without an employee record get an empty list. */
    @Transactional(readOnly = true)
    public List<PayStatementResponse> myPayStatements(String username) {
        UserAccount u = current(username);
        return u.getEmployee() == null ? List.of() : payroll.statementsFor(u.getEmployee().getId());
    }

    @Transactional
    public void changePassword(String username, PasswordChangeRequest r) {
        UserAccount u = current(username);
        if (!passwordEncoder.matches(r.currentPassword(), u.getPasswordHash())) {
            throw new InvalidRequestException("current password is incorrect");
        }
        u.changePasswordHash(passwordEncoder.encode(r.newPassword()));
        audit.record("PASSWORD_CHANGED", AuditService.USER, u.getId(), null);
    }

    private UserAccount current(String username) {
        return users.findByUsername(username).orElseThrow(() -> new NotFoundException("user " + username));
    }

    private Employee employeeOf(UserAccount u) {
        return employees.findDetailedById(u.getEmployee().getId())
                .orElseThrow(() -> new NotFoundException("employee record missing"));
    }
}
