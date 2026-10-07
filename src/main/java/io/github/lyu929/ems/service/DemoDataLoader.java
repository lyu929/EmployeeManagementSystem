package io.github.lyu929.ems.service;

import io.github.lyu929.ems.config.AppProperties;
import io.github.lyu929.ems.domain.Employee;
import io.github.lyu929.ems.domain.Role;
import io.github.lyu929.ems.domain.UserAccount;
import io.github.lyu929.ems.repo.DivisionRepository;
import io.github.lyu929.ems.repo.EmployeeRepository;
import io.github.lyu929.ems.repo.JobTitleRepository;
import io.github.lyu929.ems.repo.PayStatementRepository;
import io.github.lyu929.ems.repo.UserAccountRepository;
import io.github.lyu929.ems.security.SsnProtector;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads the course data set (15 employees, their divisions, job titles, pay history and logins) into an
 * empty database. Enabled by {@code app.demo-data.enabled=true}, which only the dev/test profiles set.
 */
@Component
@Order(1)
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class DemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    /** username, first, last, e-mail, hire date, salary, SSN, job title, division (null = none). */
    record Seed(String username, String first, String last, String email, String hired, String salary, String ssn,
            int jobTitle, Integer division) {}

    static final List<Seed> SEEDS = List.of(
            new Seed("snoopy", "Snoopy", "Beagle", "Snoopy@example.com", "2022-08-01", "45000.00", "111-11-1111", 902, 999),
            new Seed("charlie", "Charlie", "Brown", "Charlie@example.com", "2022-07-01", "48000.00", "111-22-1111", 900, 999),
            new Seed("lucy", "Lucy", "Doctor", "Lucy@example.com", "2022-07-03", "55000.00", "111-33-1111", 901, 999),
            new Seed("pepermint", "Pepermint", "Patti", "Peppermint@example.com", "2022-08-02", "98000.00", "111-44-1111", 102, null),
            new Seed("linus", "Linus", "Blanket", "Linus@example.com", "2022-09-01", "43000.00", "111-55-1111", 101, null),
            new Seed("pigpin", "PigPin", "Dusty", "PigPin@example.com", "2022-10-01", "33000.00", "111-66-1111", 201, null),
            new Seed("scooby", "Scooby", "Doo", "Scooby@example.com", "1973-07-03", "78000.00", "111-77-1111", 100, 1),
            new Seed("shaggy", "Shaggy", "Rodgers", "Shaggy@example.com", "1973-07-11", "77000.00", "111-88-1111", 102, null),
            new Seed("velma", "Velma", "Dinkley", "Velma@example.com", "1973-07-21", "82000.00", "111-99-1111", 102, null),
            new Seed("daphne", "Daphne", "Blake", "Daphne@example.com", "1973-07-30", "59000.00", "111-00-1111", 102, 1),
            new Seed("bugs", "Bugs", "Bunny", "Bugs@example.com", "1934-07-01", "18000.00", "222-11-1111", 200, null),
            new Seed("daffy", "Daffy", "Duck", "Daffy@example.com", "1935-04-01", "16000.00", "333-11-1111", 201, null),
            new Seed("porky", "Porky", "Pig", "Porky@example.com", "1935-08-12", "16550.00", "444-11-1111", 202, null),
            new Seed("elmer", "Elmer", "Fudd", "Elmer@example.com", "1934-08-01", "15500.00", "555-11-1111", 103, null),
            new Seed("marvin", "Marvin", "Martian", "Marvin@example.com", "1937-05-01", "28000.00", "777-11-1111", 103, null));

    /** Employees 1-6 have pay history for these two dates (as in the course data set). */
    static final List<LocalDate> PAY_DATES = List.of(LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 31));
    static final int EMPLOYEES_WITH_PAY = 6;

    private final AppProperties properties;
    private final EmployeeRepository employees;
    private final DivisionRepository divisions;
    private final JobTitleRepository jobTitles;
    private final PayStatementRepository statements;
    private final UserAccountRepository users;
    private final SsnProtector ssnProtector;
    private final PasswordEncoder passwordEncoder;

    public DemoDataLoader(AppProperties properties, EmployeeRepository employees, DivisionRepository divisions,
            JobTitleRepository jobTitles, PayStatementRepository statements, UserAccountRepository users,
            SsnProtector ssnProtector, PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.employees = employees;
        this.divisions = divisions;
        this.jobTitles = jobTitles;
        this.statements = statements;
        this.users = users;
        this.ssnProtector = ssnProtector;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (employees.count() > 0 || users.count() > 0) {
            return;
        }
        log.warn("Loading DEMO data with well-known passwords. Never enable app.demo-data in production.");
        String employeeHash = passwordEncoder.encode(properties.demoData().employeePassword());
        users.save(new UserAccount("admin", passwordEncoder.encode(properties.demoData().adminPassword()),
                Role.HR_ADMIN, null));
        int index = 0;
        for (Seed s : SEEDS) {
            Employee e = new Employee(s.first(), s.last(), s.email(), LocalDate.parse(s.hired()),
                    new BigDecimal(s.salary()));
            e.setSsn(ssnProtector.encrypt(s.ssn()), ssnProtector.blindIndex(s.ssn()), SsnProtector.last4(s.ssn()));
            e.assign(s.division() == null ? null : divisions.getReferenceById(s.division()),
                    jobTitles.getReferenceById(s.jobTitle()));
            employees.save(e);
            users.save(new UserAccount(s.username(), employeeHash, Role.EMPLOYEE, e));
            if (index++ < EMPLOYEES_WITH_PAY) {
                for (LocalDate d : PAY_DATES) {
                    statements.save(PayrollCalculator.statementFor(e, d));
                }
            }
        }
        log.info("Demo data loaded: {} employees, {} logins, {} pay statements", employees.count(), users.count(),
                statements.count());
    }
}
