package io.github.lyu929.ems.service;

import io.github.lyu929.ems.domain.Employee;
import io.github.lyu929.ems.domain.PayStatement;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Weekly pay statement: gross = annual salary / 52, deductions as fixed rates of gross.
 * The rates are the ones used by the course data set.
 */
public final class PayrollCalculator {

    public static final BigDecimal PAY_PERIODS = BigDecimal.valueOf(52);
    public static final BigDecimal FED_TAX = new BigDecimal("0.32");
    public static final BigDecimal FED_MED = new BigDecimal("0.0145");
    public static final BigDecimal FED_SS = new BigDecimal("0.062");
    public static final BigDecimal STATE_TAX = new BigDecimal("0.12");
    public static final BigDecimal RETIRE_401K = new BigDecimal("0.004");
    public static final BigDecimal HEALTH_CARE = new BigDecimal("0.031");

    private PayrollCalculator() {}

    public static BigDecimal grossPerPeriod(BigDecimal annualSalary) {
        return annualSalary.divide(PAY_PERIODS, Money.SCALE, RoundingMode.HALF_EVEN);
    }

    public static PayStatement statementFor(Employee employee, LocalDate payDate) {
        BigDecimal gross = grossPerPeriod(employee.getSalary());
        return new PayStatement(employee, payDate, gross,
                Money.times(gross, FED_TAX),
                Money.times(gross, FED_MED),
                Money.times(gross, FED_SS),
                Money.times(gross, STATE_TAX),
                Money.times(gross, RETIRE_401K),
                Money.times(gross, HEALTH_CARE));
    }
}
