package io.github.lyu929.ems.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lyu929.ems.domain.Employee;
import io.github.lyu929.ems.domain.PayStatement;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MoneyAndPayrollTest {

    @Test
    void percentagesUseBankersRoundingOnCents() {
        assertThat(Money.applyPercent(new BigDecimal("45000.00"), new BigDecimal("3"))).isEqualByComparingTo("46350.00");
        assertThat(Money.applyPercent(new BigDecimal("100.00"), new BigDecimal("0.005"))).isEqualByComparingTo("100.00");
        assertThat(Money.applyPercent(new BigDecimal("100.00"), new BigDecimal("0.015"))).isEqualByComparingTo("100.02");
        assertThat(Money.applyPercent(new BigDecimal("50000.00"), new BigDecimal("-10"))).isEqualByComparingTo("45000.00");
        assertThat(Money.applyPercent(new BigDecimal("1.00"), BigDecimal.ZERO).scale()).isEqualTo(2);
    }

    @Test
    void weeklyStatementMatchesTheCourseRates() {
        Employee e = new Employee("Snoopy", "Beagle", "s@example.com", LocalDate.of(2022, 8, 1),
                new BigDecimal("45000.00"));
        PayStatement p = PayrollCalculator.statementFor(e, LocalDate.of(2026, 1, 31));
        assertThat(p.getEarnings()).isEqualByComparingTo("865.38"); // 45000 / 52
        assertThat(p.getFedTax()).isEqualByComparingTo("276.92");   // 32 %
        assertThat(p.getFedMed()).isEqualByComparingTo("12.55");    // 1.45 %
        assertThat(p.getFedSs()).isEqualByComparingTo("53.65");     // 6.2 %
        assertThat(p.getStateTax()).isEqualByComparingTo("103.85"); // 12 %
        assertThat(p.getRetire401k()).isEqualByComparingTo("3.46"); // 0.4 %
        assertThat(p.getHealthCare()).isEqualByComparingTo("26.83");// 3.1 %
        assertThat(p.netPay()).isEqualByComparingTo(p.getEarnings().subtract(p.totalDeductions()));
    }

    @Test
    void likeWildcardsAreEscaped() {
        assertThat(EmployeeService.escapeLike("50%_off!")).isEqualTo("50!%!_off!!");
    }
}
