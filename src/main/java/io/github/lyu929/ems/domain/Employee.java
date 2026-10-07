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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An employee. The SSN is never stored in clear text: {@code ssnCiphertext} is AES-GCM encrypted,
 * {@code ssnHash} is a keyed hash used for exact-match search, {@code ssnLast4} is used for masking.
 */
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;
    private String lastName;
    private String email;
    private LocalDate hireDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal salary;

    private String ssnCiphertext;
    private String ssnHash;

    @Column(name = "ssn_last4")
    private String ssnLast4;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id")
    private Division division;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_id")
    private JobTitle jobTitle;

    /** Optimistic locking: concurrent edits of the same employee fail instead of overwriting. */
    @Version
    private long version;

    protected Employee() {}

    public Employee(String firstName, String lastName, String email, LocalDate hireDate, BigDecimal salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.hireDate = hireDate;
        this.salary = salary;
    }

    public void rename(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public void setSsn(String ciphertext, String hash, String last4) {
        this.ssnCiphertext = ciphertext;
        this.ssnHash = hash;
        this.ssnLast4 = last4;
    }

    public void assign(Division division, JobTitle jobTitle) {
        this.division = division;
        this.jobTitle = jobTitle;
    }

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public String getSsnCiphertext() {
        return ssnCiphertext;
    }

    public String getSsnHash() {
        return ssnHash;
    }

    public String getSsnLast4() {
        return ssnLast4;
    }

    public Division getDivision() {
        return division;
    }

    public JobTitle getJobTitle() {
        return jobTitle;
    }

    public long getVersion() {
        return version;
    }
}
