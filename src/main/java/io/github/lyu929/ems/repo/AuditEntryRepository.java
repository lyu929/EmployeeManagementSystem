package io.github.lyu929.ems.repo;

import io.github.lyu929.ems.domain.AuditEntry;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {

    Page<AuditEntry> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(String entityType, String entityId,
            Pageable pageable);

    Page<AuditEntry> findAllByOrderByOccurredAtDesc(Pageable pageable);

    List<AuditEntry> findByAction(String action);
}
