package io.github.lyu929.ems.web;

import io.github.lyu929.ems.repo.DivisionRepository;
import io.github.lyu929.ems.repo.JobTitleRepository;
import io.github.lyu929.ems.service.AuditService;
import io.github.lyu929.ems.service.Mappers;
import io.github.lyu929.ems.service.UserService;
import io.github.lyu929.ems.web.dto.AuditResponse;
import io.github.lyu929.ems.web.dto.CreateUserRequest;
import io.github.lyu929.ems.web.dto.PageResponse;
import io.github.lyu929.ems.web.dto.Ref;
import io.github.lyu929.ems.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Reference data, login management and the audit trail. */
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('HR_ADMIN')")
@Validated
@Tag(name = "Administration (HR admin)")
public class AdminController {

    private final DivisionRepository divisions;
    private final JobTitleRepository jobTitles;
    private final UserService users;
    private final AuditService audit;

    public AdminController(DivisionRepository divisions, JobTitleRepository jobTitles, UserService users,
            AuditService audit) {
        this.divisions = divisions;
        this.jobTitles = jobTitles;
        this.users = users;
        this.audit = audit;
    }

    @GetMapping("/divisions")
    @Transactional(readOnly = true)
    public List<Ref> divisions() {
        return divisions.findAll(Sort.by("id")).stream().map(Mappers::division).toList();
    }

    @GetMapping("/job-titles")
    @Transactional(readOnly = true)
    public List<Ref> jobTitles() {
        return jobTitles.findAll(Sort.by("id")).stream().map(Mappers::jobTitle).toList();
    }

    @PostMapping("/users")
    @Operation(summary = "Create a login (EMPLOYEE logins must be linked to an employee)")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = users.create(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
    }

    @GetMapping("/audit")
    @Operation(summary = "Audit trail, newest first; filter with entityType + entityId")
    public PageResponse<AuditResponse> audit(
            @RequestParam(name = "entityType", required = false) String entityType,
            @RequestParam(name = "entityId", required = false) String entityId,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "50") @Min(1) @Max(200) int size) {
        return audit.list(entityType, entityId, PageRequest.of(page, size));
    }
}
