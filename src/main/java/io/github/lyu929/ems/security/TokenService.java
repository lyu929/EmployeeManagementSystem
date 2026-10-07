package io.github.lyu929.ems.security;

import io.github.lyu929.ems.config.AppProperties;
import io.github.lyu929.ems.domain.UserAccount;
import io.github.lyu929.ems.web.dto.TokenResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Issues short-lived HS256 access tokens carrying the user's role (and employee id, if any). */
@Service
public class TokenService {

    public static final String ISSUER = "company-z-ems";
    public static final String ROLES_CLAIM = "roles";
    public static final String EMPLOYEE_CLAIM = "employeeId";

    private final JwtEncoder encoder;
    private final Duration ttl;
    private final Clock clock;

    public TokenService(JwtEncoder encoder, AppProperties properties, Clock clock) {
        this.encoder = encoder;
        this.ttl = properties.security().tokenTtl();
        this.clock = clock;
    }

    public TokenResponse issue(UserAccount user) {
        Instant now = clock.instant();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(user.getUsername())
                .claim(ROLES_CLAIM, List.of(user.getRole().name()));
        if (user.getEmployee() != null) {
            claims.claim(EMPLOYEE_CLAIM, user.getEmployee().getId());
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new TokenResponse(token, "Bearer", ttl.toSeconds(), user.getRole().name(), user.getUsername());
    }
}
