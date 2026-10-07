package io.github.lyu929.ems.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** One pay period for one employee: gross earnings and itemised deductions. */
@Entity
@Table(name = "pay_statement")
public class PayStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDate payDate;
    private BigDecimal earnings;
    private BigDecimal fedTax;
    private BigDecimal fedMed;

    @Column(name = "fed_ss")
    private BigDecimal fedSs;

    private BigDecimal stateTax;

    @Column(name = "retire_401k")
    private BigDecimal retire401k;

    private BigDecimal healthCare;

    protected PayStatement() {}

    public PayStatement(Employee employee, LocalDate payDate, BigDecimal earnings, BigDecimal fedTax,
            BigDecimal fedMed, BigDecimal fedSs, BigDecimal stateTax, BigDecimal retire401k, BigDecimal healthCare) {
        this.employee = employee;
        this.payDate = payDate;
        this.earnings = earnings;
        this.fedTax = fedTax;
        this.fedMed = fedMed;
        this.fedSs = fedSs;
        this.stateTax = stateTax;
        this.retire401k = retire401k;
        this.healthCare = healthCare;
    }

    public BigDecimal totalDeductions() {
        return fedTax.add(fedMed).add(fedSs).add(stateTax).add(retire401k).add(healthCare);
    }

    public BigDecimal netPay() {
        return earnings.subtract(totalDeductions());
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public LocalDate getPayDate() {
        return payDate;
    }

    public BigDecimal getEarnings() {
        return earnings;
    }

    public BigDecimal getFedTax() {
        return fedTax;
    }

    public BigDecimal getFedMed() {
        return fedMed;
    }

    public BigDecimal getFedSs() {
        return fedSs;
    }

    public BigDecimal getStateTax() {
        return stateTax;
    }

    public BigDecimal getRetire401k() {
        return retire401k;
    }

    public BigDecimal getHealthCare() {
        return healthCare;
    }
}
