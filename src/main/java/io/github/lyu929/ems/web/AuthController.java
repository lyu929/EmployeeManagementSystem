package io.github.lyu929.ems.web;

import io.github.lyu929.ems.service.AuthService;
import io.github.lyu929.ems.web.dto.LoginRequest;
import io.github.lyu929.ems.web.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    @Operation(summary = "Exchange username and password for a bearer token")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }
}
