package io.github.lyu929.ems.service;

import io.github.lyu929.ems.config.AppProperties;
import io.github.lyu929.ems.domain.Role;
import io.github.lyu929.ems.domain.UserAccount;
import io.github.lyu929.ems.repo.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first HR admin from EMS_ADMIN_USERNAME / EMS_ADMIN_PASSWORD when the user table is empty,
 * so a production database never needs a hard-coded account.
 */
@Component
@Order(2)
public class BootstrapAdmin implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdmin.class);

    private final AppProperties properties;
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;

    public BootstrapAdmin(AppProperties properties, UserAccountRepository users, PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppProperties.BootstrapAdmin admin = properties.bootstrapAdmin();
        if (admin == null || admin.username() == null || admin.username().isBlank() || users.count() > 0) {
            return;
        }
        if (admin.password() == null || admin.password().length() < 10) {
            throw new IllegalStateException("EMS_ADMIN_PASSWORD must be at least 10 characters");
        }
        users.save(new UserAccount(admin.username().trim(), passwordEncoder.encode(admin.password()), Role.HR_ADMIN,
                null));
        log.info("Created initial HR admin '{}'", admin.username().trim());
    }
}
