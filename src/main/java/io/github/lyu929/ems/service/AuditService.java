package io.github.lyu929.ems.service;

import io.github.lyu929.ems.domain.AuditEntry;
import io.github.lyu929.ems.repo.AuditEntryRepository;
import io.github.lyu929.ems.security.CurrentUser;
import io.github.lyu929.ems.web.dto.AuditResponse;
import io.github.lyu929.ems.web.dto.PageResponse;
import java.time.Clock;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Writes the audit trail inside the caller's transaction, so a rolled-back change leaves no entry. */
@Service
public class AuditService {

    public static final String EMPLOYEE = "EMPLOYEE";
    public static final String USER = "USER";
    public static final String PAYROLL = "PAYROLL";

    private final AuditEntryRepository repository;
    private final Clock clock;

    public AuditService(AuditEntryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void record(String action, String entityType, Object entityId, String details) {
        String detail = details != null && details.length() > 1000 ? details.substring(0, 997) + "..." : details;
        repository.save(new AuditEntry(clock.instant(), CurrentUser.name(), action, entityType,
                entityId == null ? null : String.valueOf(entityId), detail));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditResponse> list(String entityType, String entityId, Pageable pageable) {
        if (entityType != null && entityId != null) {
            return PageResponse.of(repository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(entityType, entityId,
                    pageable), Mappers::audit);
        }
        return PageResponse.of(repository.findAllByOrderByOccurredAtDesc(pageable), Mappers::audit);
    }
}
