package in.crewplay.crewplay_backend.domain.user.repository;

import in.crewplay.crewplay_backend.domain.user.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 📁 src/main/java/in/crewplay/crewplay_backend/domain/user/repository/PlayerProfileRepository.java
 * Action: CREATE this file — it didn't exist. Used by ManagerRecruitService to
 *         show player names, playing roles, and profile images on discover cards.
 */
public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {

    /**
     * Bulk-fetch profiles for a list of user IDs.
     * Used to enrich discover cards without N+1 queries.
     */
    List<PlayerProfile> findByIdIn(List<Long> userIds);
}