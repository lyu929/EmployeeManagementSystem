package io.github.lyu929.ems.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lyu929.ems.domain.Employee;
import io.github.lyu929.ems.repo.AuditEntryRepository;
import io.github.lyu929.ems.repo.EmployeeRepository;
import io.github.lyu929.ems.service.PayrollCalculator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * End-to-end API tests against the demo data set (H2 in MySQL mode, Flyway migrations, real security
 * filter chain). Each test runs in a transaction that is rolled back afterwards.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private EmployeeRepository employees;

    @Autowired
    private AuditEntryRepository auditEntries;

    // ------------------------------------------------------------------ helpers

    private String token(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("accessToken").asText();
    }

    private MockHttpServletRequestBuilder as(String token, MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + token);
    }

    private String admin() throws Exception {
        return token("admin", "admin123");
    }

    private MockHttpServletRequestBuilder jsonBody(MockHttpServletRequestBuilder request, Object body)
            throws Exception {
        return request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
    }

    private Map<String, Object> newEmployee(String email) {
        return new java.util.HashMap<>(Map.of(
                "firstName", "Ada", "lastName", "Lovelace", "email", email, "hireDate", "2026-02-01",
                "salary", "120000.00", "ssn", "987-65-4321", "divisionId", 1, "jobTitleId", 101));
    }

    // ------------------------------------------------------------------ authentication

    @Test
    void loginReturnsABearerTokenWithTheRole() throws Exception {
        mvc.perform(jsonBody(post("/api/v1/auth/login"), Map.of("username", "admin", "password", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("HR_ADMIN"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void wrongPasswordAndUnknownUserGiveTheSameAnswer() throws Exception {
        for (Map<String, String> creds : List.of(Map.of("username", "charlie", "password", "nope"),
                Map.of("username", "nobody", "password", "nope"))) {
            mvc.perform(jsonBody(post("/api/v1/auth/login"), creds))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value("Invalid username or password"));
        }
    }

    @Test
    void repeatedFailuresLockTheAccountTemporarily() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(jsonBody(post("/api/v1/auth/login"), Map.of("username", "lucy", "password", "guess" + i)))
                    .andExpect(status().isUnauthorized());
        }
        // even the right password is refused while locked
        mvc.perform(jsonBody(post("/api/v1/auth/login"), Map.of("username", "lucy", "password", "emp123")))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void missingTokenIs401AndEmployeesCannotUseAdminEndpoints() throws Exception {
        mvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"));
        String employee = token("snoopy", "emp123");
        mvc.perform(as(employee, get("/api/v1/employees"))).andExpect(status().isForbidden());
        mvc.perform(as(employee, get("/api/v1/reports/payroll").param("payDate", "2026-01-31")))
                .andExpect(status().isForbidden());
        mvc.perform(as("not-a-jwt", get("/api/v1/me"))).andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------ self-service

    @Test
    void employeesSeeOnlyTheirOwnMaskedRecordAndPayHistory() throws Exception {
        String snoopy = token("snoopy", "emp123");
        mvc.perform(as(snoopy, get("/api/v1/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("snoopy"))
                .andExpect(jsonPath("$.employee.firstName").value("Snoopy"))
                .andExpect(jsonPath("$.employee.ssn").value("***-**-1111"))
                .andExpect(jsonPath("$.employee.division.name").value("HQ"));
        mvc.perform(as(snoopy, get("/api/v1/me/pay-statements")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].payDate").value("2026-01-31"))
                .andExpect(jsonPath("$[0].earnings").value(865.38));
    }

    @Test
    void passwordChangeRequiresTheCurrentPassword() throws Exception {
        String velma = token("velma", "emp123");
        mvc.perform(jsonBody(as(velma, put("/api/v1/me/password")),
                        Map.of("currentPassword", "wrong", "newPassword", "a-much-longer-secret")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("current password is incorrect"));
        mvc.perform(jsonBody(as(velma, put("/api/v1/me/password")),
                        Map.of("currentPassword", "emp123", "newPassword", "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("newPassword"));
        mvc.perform(jsonBody(as(velma, put("/api/v1/me/password")),
                        Map.of("currentPassword", "emp123", "newPassword", "a-much-longer-secret")))
                .andExpect(status().isNoContent());
        token("velma", "a-much-longer-secret");
    }

    // ------------------------------------------------------------------ employees

    @Test
    void searchIsPagedCaseInsensitiveAndNeverLeaksSsns() throws Exception {
        String admin = admin();
        mvc.perform(as(admin, get("/api/v1/employees").param("size", "5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[*].ssn", everyItem(startsWith("***-**-"))));
        mvc.perform(as(admin, get("/api/v1/employees").param("q", "BUN")))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].lastName").value("Bunny"));
        mvc.perform(as(admin, get("/api/v1/employees").param("q", "%")))
                .andExpect(jsonPath("$.totalElements").value(0)); // wildcards are literal
        mvc.perform(as(admin, get("/api/v1/employees").param("size", "500")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ssnSearchUsesTheBlindIndexAndIsAudited() throws Exception {
        String admin = admin();
        mvc.perform(jsonBody(as(admin, post("/api/v1/employees/search/ssn")), Map.of("ssn", "111-77-1111")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Scooby"))
                .andExpect(jsonPath("$.ssn").value("***-**-1111"));
        mvc.perform(jsonBody(as(admin, post("/api/v1/employees/search/ssn")), Map.of("ssn", "999-99-9999")))
                .andExpect(status().isNotFound());
        mvc.perform(jsonBody(as(admin, post("/api/v1/employees/search/ssn")), Map.of("ssn", "12345")))
                .andExpect(status().isBadRequest());
        assertThat(auditEntries.findByAction("SSN_SEARCH")).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void ssnIsEncryptedAtRestAndRevealIsAudited() throws Exception {
        Employee scooby = employees.findById(7).orElseThrow();
        assertThat(scooby.getSsnCiphertext()).isNotBlank().doesNotContain("111-77-1111").doesNotContain("111771111");
        mvc.perform(as(admin(), get("/api/v1/employees/7/ssn")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ssn").value("111-77-1111"));
        assertThat(auditEntries.findByAction("SSN_VIEWED")).anyMatch(a -> "7".equals(a.getEntityId())
                && "admin".equals(a.getActor()));
    }

    @Test
    void createValidatesAndRejectsDuplicates() throws Exception {
        String admin = admin();
        mvc.perform(jsonBody(as(admin, post("/api/v1/employees")),
                        Map.of("firstName", "", "email", "not-an-email", "salary", "-5", "ssn", "12")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("firstName")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("lastName")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("email")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("salary")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("ssn")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("hireDate")));

        Map<String, Object> ada = newEmployee("ada@example.com");
        String created = mvc.perform(jsonBody(as(admin, post("/api/v1/employees")), ada))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/employees/")))
                .andExpect(jsonPath("$.ssn").value("***-**-4321"))
                .andExpect(jsonPath("$.division.name").value("Technology Engineering"))
                .andExpect(jsonPath("$.jobTitle.name").value("Software Architect"))
                .andReturn().getResponse().getContentAsString();
        assertThat(created).doesNotContain("987-65-4321");

        mvc.perform(jsonBody(as(admin, post("/api/v1/employees")), ada))
                .andExpect(status().isConflict()); // same e-mail
        Map<String, Object> sameSsn = newEmployee("other@example.com");
        mvc.perform(jsonBody(as(admin, post("/api/v1/employees")), sameSsn))
                .andExpect(status().isConflict());
        Map<String, Object> badDivision = newEmployee("third@example.com");
        badDivision.put("ssn", "222-33-4444");
        badDivision.put("divisionId", 12345);
        mvc.perform(jsonBody(as(admin, post("/api/v1/employees")), badDivision))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("division 12345")));
        assertThat(auditEntries.findByAction("EMPLOYEE_CREATED")).hasSize(1);
    }

    @Test
    void updateRecordsWhatChanged() throws Exception {
        Map<String, Object> body = new java.util.HashMap<>(Map.of(
                "firstName", "Charlie", "lastName", "Brown", "email", "Charlie@example.com", "hireDate", "2022-07-01",
                "salary", "50000.00", "divisionId", 2, "jobTitleId", 900));
        mvc.perform(jsonBody(as(admin(), put("/api/v1/employees/2")), body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salary").value(50000.00))
                .andExpect(jsonPath("$.division.name").value("Marketing"))
                .andExpect(jsonPath("$.ssn").value("***-**-1111")); // kept
        assertThat(auditEntries.findByAction("EMPLOYEE_UPDATED"))
                .anyMatch(a -> a.getDetails().contains("salary 48000.00 -> 50000.00")
                        && a.getDetails().contains("division 999 -> 2"));
    }

    @Test
    void deleteRemovesTheEmployeeTheirPayHistoryAndLogin() throws Exception {
        String admin = admin();
        mvc.perform(as(admin, delete("/api/v1/employees/1"))).andExpect(status().isNoContent());
        mvc.perform(as(admin, get("/api/v1/employees/1"))).andExpect(status().isNotFound());
        mvc.perform(jsonBody(post("/api/v1/auth/login"), Map.of("username", "snoopy", "password", "emp123")))
                .andExpect(status().isUnauthorized());
        mvc.perform(as(admin, get("/api/v1/employees/1/pay-statements"))).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ salaries and payroll

    @Test
    void salaryAdjustmentPreviewsThenApplies() throws Exception {
        String admin = admin();
        Map<String, Object> hq = Map.of("percent", "3", "divisionId", 999, "dryRun", true);
        mvc.perform(jsonBody(as(admin, post("/api/v1/salary-adjustments")), hq))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applied").value(false))
                .andExpect(jsonPath("$.affected").value(3));
        assertThat(employees.findById(1).orElseThrow().getSalary()).isEqualByComparingTo("45000.00");

        mvc.perform(jsonBody(as(admin, post("/api/v1/salary-adjustments")),
                        Map.of("percent", "3", "divisionId", 999, "dryRun", false)))
                .andExpect(jsonPath("$.applied").value(true))
                .andExpect(jsonPath("$.changes[0].newSalary").value(46350.00));
        assertThat(employees.findById(1).orElseThrow().getSalary()).isEqualByComparingTo("46350.00");
        assertThat(auditEntries.findByAction("SALARY_CHANGED")).hasSize(3);

        mvc.perform(jsonBody(as(admin, post("/api/v1/salary-adjustments")),
                        Map.of("percent", "5", "minSalary", "20000", "maxSalary", "30000", "dryRun", true)))
                .andExpect(jsonPath("$.affected").value(1)); // only Marvin (28000)
    }

    @Test
    void salaryAdjustmentNeedsAFilterAndAnOrderedRange() throws Exception {
        String admin = admin();
        mvc.perform(jsonBody(as(admin, post("/api/v1/salary-adjustments")), Map.of("percent", "3")))
                .andExpect(status().isBadRequest());
        mvc.perform(jsonBody(as(admin, post("/api/v1/salary-adjustments")),
                        Map.of("percent", "3", "minSalary", "90000", "maxSalary", "10000")))
                .andExpect(status().isBadRequest());
        mvc.perform(jsonBody(as(admin, post("/api/v1/salary-adjustments")),
                        Map.of("percent", "500", "divisionId", 1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payrollRunsAreIdempotent() throws Exception {
        String admin = admin();
        mvc.perform(jsonBody(as(admin, post("/api/v1/payroll/runs")), Map.of("payDate", "2026-02-28")))
                .andExpect(jsonPath("$.created").value(15))
                .andExpect(jsonPath("$.skipped").value(0));
        mvc.perform(jsonBody(as(admin, post("/api/v1/payroll/runs")), Map.of("payDate", "2026-02-28")))
                .andExpect(jsonPath("$.created").value(0))
                .andExpect(jsonPath("$.skipped").value(15));
    }

    // ------------------------------------------------------------------ reports

    @Test
    void payrollSummaryAddsUpTheStatements() throws Exception {
        BigDecimal expected = BigDecimal.ZERO;
        for (int id = 1; id <= 6; id++) {
            expected = expected.add(PayrollCalculator.grossPerPeriod(employees.findById(id).orElseThrow().getSalary()));
        }
        String body = mvc.perform(as(admin(), get("/api/v1/reports/payroll").param("payDate", "2026-01-31")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statements").value(6))
                .andReturn().getResponse().getContentAsString();
        JsonNode r = json.readTree(body);
        assertThat(r.get("totalEarnings").decimalValue()).isEqualByComparingTo(expected);
        assertThat(r.get("netTotal").decimalValue())
                .isEqualByComparingTo(r.get("totalEarnings").decimalValue().subtract(r.get("totalDeductions").decimalValue()));
    }

    @Test
    void periodReportsGroupAndShowUnassigned() throws Exception {
        String admin = admin();
        mvc.perform(as(admin, get("/api/v1/reports/pay-by-division").param("month", "2026-01")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[*].group", hasItem("HQ")))
                .andExpect(jsonPath("$.rows[*].group", hasItem("(unassigned)")))
                .andExpect(jsonPath("$.rows[*].statements", not(hasItem(0))));
        mvc.perform(as(admin, get("/api/v1/reports/pay-by-job-title").param("month", "2026-01")))
                .andExpect(jsonPath("$.rows", hasSize(6)));
        mvc.perform(as(admin, get("/api/v1/reports/pay-by-job-title").param("month", "January")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void newHiresAreNewestFirst() throws Exception {
        String admin = admin();
        mvc.perform(as(admin, get("/api/v1/reports/new-hires").param("from", "2022-01-01").param("to", "2022-12-31")))
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].firstName").value("PigPin"))
                .andExpect(jsonPath("$[5].firstName").value("Charlie"));
        mvc.perform(as(admin, get("/api/v1/reports/new-hires").param("from", "2023-01-01").param("to", "2022-01-01")))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------ platform

    @Test
    void docsHealthAndUiArePublic() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Company Z Employee Management API")));
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/index.html")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/divisions")).andExpect(status().isUnauthorized());
        mvc.perform(as(admin(), get("/api/v1/audit"))).andExpect(status().isOk());
    }
}
