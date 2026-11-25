package fptu.sba301.ats.config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Shared JPA query helpers for dev database seeders.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
class SeedQueryHelper {

    private final EntityManager entityManager;

    <T> boolean isEntityEmpty(Class<T> entityClass) {
        try {
            Long count = entityManager.createQuery(
                    "select count(e) from " + entityClass.getSimpleName() + " e",
                    Long.class
            ).getSingleResult();
            return count == 0;
        } catch (Exception ex) {
            log.warn("Skip seeding {} because table/entity is not ready: {}", entityClass.getSimpleName(), ex.getMessage());
            return false;
        }
    }

    <T> List<T> findAll(Class<T> entityClass) {
        return entityManager.createQuery(
                "select e from " + entityClass.getSimpleName() + " e",
                entityClass
        ).getResultList();
    }
}
