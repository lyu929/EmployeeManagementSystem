package io.github.lyu929.ems.web;

import io.github.lyu929.ems.error.InvalidRequestException;
import io.github.lyu929.ems.service.ReportService;
import io.github.lyu929.ems.web.dto.NewHireResponse;
import io.github.lyu929.ems.web.dto.PayrollSummaryResponse;
import io.github.lyu929.ems.web.dto.PeriodReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('HR_ADMIN')")
@Tag(name = "Reports (HR admin)")
public class ReportController {

    private final ReportService reports;

    public ReportController(ReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/payroll")
    @Operation(summary = "Total earnings, deductions and net pay for one pay date")
    public PayrollSummaryResponse payroll(
            @RequestParam("payDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate payDate) {
        return reports.payrollSummary(payDate);
    }

    @GetMapping("/pay-by-job-title")
    @Operation(summary = "Total pay per job title for a month (YYYY-MM)")
    public PeriodReportResponse payByJobTitle(@RequestParam("month") String month) {
        return reports.payByJobTitle(parseMonth(month));
    }

    @GetMapping("/pay-by-division")
    @Operation(summary = "Total pay per division for a month (YYYY-MM)")
    public PeriodReportResponse payByDivision(@RequestParam("month") String month) {
        return reports.payByDivision(parseMonth(month));
    }

    @GetMapping("/new-hires")
    @Operation(summary = "Employees hired in a date range, newest first")
    public List<NewHireResponse> newHires(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reports.newHires(from, to);
    }

    private static YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("month must look like 2026-01");
        }
    }
}
