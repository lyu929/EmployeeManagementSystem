package io.github.lyu929.ems.web;

import io.github.lyu929.ems.service.UserService;
import io.github.lyu929.ems.web.dto.MeResponse;
import io.github.lyu929.ems.web.dto.PasswordChangeRequest;
import io.github.lyu929.ems.web.dto.PayStatementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Employee self-service: every authenticated user, only their own data. */
@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Self-service")
public class MeController {

    private final UserService users;

    public MeController(UserService users) {
        this.users = users;
    }

    @GetMapping
    @Operation(summary = "The logged-in user and their employee record (SSN masked)")
    public MeResponse me(Principal principal) {
        return users.me(principal.getName());
    }

    @GetMapping("/pay-statements")
    @Operation(summary = "Own pay statement history, newest first")
    public List<PayStatementResponse> payStatements(Principal principal) {
        return users.myPayStatements(principal.getName());
    }

    @PutMapping("/password")
    @Operation(summary = "Change own password")
    public ResponseEntity<Void> changePassword(Principal principal, @Valid @RequestBody PasswordChangeRequest request) {
        users.changePassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
