package io.github.lyu929.ems.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Percentage raise for every employee matching the filters (salary range and/or division).
 *
 * @param dryRun when true, only report who would be affected
 */
public record SalaryAdjustmentRequest(
        @NotNull @DecimalMin("-50") @DecimalMax("100") BigDecimal percent,
        @DecimalMin("0") BigDecimal minSalary,
        @DecimalMin("0") BigDecimal maxSalary,
        Integer divisionId,
        boolean dryRun) {

    @JsonIgnore
    @AssertTrue(message = "give a salary range and/or a division")
    public boolean isFiltered() {
        return minSalary != null || maxSalary != null || divisionId != null;
    }

    @JsonIgnore
    @AssertTrue(message = "minSalary must not exceed maxSalary")
    public boolean isRangeOrdered() {
        return minSalary == null || maxSalary == null || minSalary.compareTo(maxSalary) <= 0;
    }
}
