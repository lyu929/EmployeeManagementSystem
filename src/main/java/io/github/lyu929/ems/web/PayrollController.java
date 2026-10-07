package io.github.lyu929.ems.web;

import io.github.lyu929.ems.service.PayrollService;
import io.github.lyu929.ems.service.SalaryService;
import io.github.lyu929.ems.web.dto.PayrollRunRequest;
import io.github.lyu929.ems.web.dto.PayrollRunResponse;
import io.github.lyu929.ems.web.dto.SalaryAdjustmentRequest;
import io.github.lyu929.ems.web.dto.SalaryAdjustmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('HR_ADMIN')")
@Tag(name = "Payroll and salaries (HR admin)")
public class PayrollController {

    private final SalaryService salaries;
    private final PayrollService payroll;

    public PayrollController(SalaryService salaries, PayrollService payroll) {
        this.salaries = salaries;
        this.payroll = payroll;
    }

    @PostMapping("/salary-adjustments")
    @Operation(summary = "Percentage raise by salary range and/or division; dryRun previews the result")
    public SalaryAdjustmentResponse adjust(@Valid @RequestBody SalaryAdjustmentRequest request) {
        return salaries.adjust(request);
    }

    @PostMapping("/payroll/runs")
    @Operation(summary = "Create pay statements for every employee for one pay date (idempotent)")
    public PayrollRunResponse run(@Valid @RequestBody PayrollRunRequest request) {
        return payroll.run(request.payDate());
    }
}
