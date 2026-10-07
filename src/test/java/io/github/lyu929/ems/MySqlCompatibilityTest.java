package io.github.lyu929.ems;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lyu929.ems.repo.EmployeeRepository;
import io.github.lyu929.ems.repo.PayStatementRepository;
import io.github.lyu929.ems.service.ReportService;
import io.github.lyu929.ems.web.dto.GroupTotalResponse;
import io.github.lyu929.ems.web.dto.PeriodReportResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs the Flyway migrations, the demo-data loader and the report queries against a real MySQL 8.4.
 * Skipped automatically when Docker is not available (it always runs in CI).
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class MySqlCompatibilityTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private EmployeeRepository employees;

    @Autowired
    private PayStatementRepository statements;

    @Autowired
    private ReportService reports;

    @Test
    void schemaSeedAndReportsWorkOnMySql() {
        assertThat(employees.count()).isEqualTo(15);
        assertThat(statements.count()).isEqualTo(12);
        assertThat(reports.payrollSummary(LocalDate.of(2026, 1, 31)).statements()).isEqualTo(6);
        PeriodReportResponse byDivision = reports.payByDivision(YearMonth.of(2026, 1));
        assertThat(byDivision.rows()).extracting(GroupTotalResponse::group).contains("HQ", "(unassigned)");
        assertThat(reports.newHires(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31))).hasSize(6);
        assertThat(employees.search("bun", org.springframework.data.domain.PageRequest.of(0, 5)).getTotalElements())
                .isEqualTo(1);
    }
}
