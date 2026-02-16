package in.crewplay.crewplay_backend.domain.match.repository;

import in.crewplay.crewplay_backend.domain.match.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 📁 src/main/java/in/crewplay/crewplay_backend/domain/match/repository/MatchRepository.java
 * Action: REPLACE your existing file — previous queries used "MatchTeam" which
 *         doesn't exist in the current Match entity. Match now has direct teamA/teamB fields.
 */
public interface MatchRepository extends JpaRepository<Match, Long> {

    /**
     * Used by: ManagerDashboardService — finds the next upcoming match card.
     *
     * "Upcoming" = match has a future scheduledAt AND is in a pre-match status.
     * MatchStatus.isPreMatch() covers: DRAFT, READY, SPECS_LOCKED,
     *                                  AWAITING_VERIFICATION, VERIFIED.
     * We exclude LIVE and COMPLETED. TOSS_DONE is still imminent so we include it.
     *
     * Returns the soonest one (ORDER BY scheduledAt ASC, LIMIT 1).
     */
    @Query("""
        SELECT m FROM Match m
        WHERE (m.teamA.id = :teamId OR m.teamB.id = :teamId)
          AND m.scheduledAt > :now
          AND m.status NOT IN ('LIVE', 'COMPLETED')
        ORDER BY m.scheduledAt ASC
        LIMIT 1
    """)
    Optional<Match> findNextUpcomingForTeam(
            @Param("teamId") Long teamId,
            @Param("now") LocalDateTime now
    );

    /**
     * Used by: ManagerAnalyticsService — last N completed matches for analytics.
     */
    @Query("""
        SELECT m FROM Match m
        WHERE (m.teamA.id = :teamId OR m.teamB.id = :teamId)
          AND m.status = 'COMPLETED'
        ORDER BY m.scheduledAt DESC
        LIMIT :limit
    """)
    List<Match> findRecentCompletedByTeam(
            @Param("teamId") Long teamId,
            @Param("limit") int limit
    );

    /**
     * Used by: ManagerAnalyticsService — full match history for stats table.
     */
    @Query("""
        SELECT m FROM Match m
        WHERE (m.teamA.id = :teamId OR m.teamB.id = :teamId)
          AND m.status = 'COMPLETED'
        ORDER BY m.scheduledAt DESC
    """)
    List<Match> findCompletedByTeam(@Param("teamId") Long teamId);
}