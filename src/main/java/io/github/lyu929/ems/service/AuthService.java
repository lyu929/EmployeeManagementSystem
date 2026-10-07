package io.github.lyu929.ems.service;

import io.github.lyu929.ems.domain.UserAccount;
import io.github.lyu929.ems.error.InvalidCredentialsException;
import io.github.lyu929.ems.error.TooManyAttemptsException;
import io.github.lyu929.ems.repo.UserAccountRepository;
import io.github.lyu929.ems.security.LoginAttemptService;
import io.github.lyu929.ems.security.TokenService;
import io.github.lyu929.ems.web.dto.LoginRequest;
import io.github.lyu929.ems.web.dto.TokenResponse;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Username/password login. Replaces the course version's plain-text password comparison in SQL with
 * BCrypt, adds a per-username lockout and answers "invalid username or password" in every failure case.
 */
@Service
public class AuthService {

    /** BCrypt hash compared against when the user does not exist, so both cases take the same time. */
    private final String dummyHash;

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService attempts;
    private final TokenService tokens;

    public AuthService(UserAccountRepository users, PasswordEncoder passwordEncoder, LoginAttemptService attempts,
            TokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.attempts = attempts;
        this.tokens = tokens;
        this.dummyHash = passwordEncoder.encode("timing-equaliser-not-a-password");
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        attempts.lockedFor(request.username()).ifPresent(left -> {
            throw new TooManyAttemptsException(left);
        });
        Optional<UserAccount> user = users.findByUsername(request.username().trim());
        String hash = user.map(UserAccount::getPasswordHash).orElse(dummyHash);
        boolean ok = passwordEncoder.matches(request.password(), hash) && user.isPresent() && user.get().isEnabled();
        if (!ok) {
            attempts.recordFailure(request.username());
            throw new InvalidCredentialsException();
        }
        attempts.recordSuccess(request.username());
        return tokens.issue(user.get());
    }
}
