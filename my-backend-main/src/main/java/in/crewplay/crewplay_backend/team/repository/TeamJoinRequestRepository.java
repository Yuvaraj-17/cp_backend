package in.crewplay.crewplay_backend.team.repository;

import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.domain.teams.TeamJoinRequest;
import in.crewplay.crewplay_backend.domain.teams.enums.JoinRequestStatus;
import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.team.enums.JoinRequestStatusForOverAllTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * repository for handling team join requests.
 * Supports both player requests and manager invites.
 */
public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Long> {

    /**
     * Fetch all requests for a team by status.
     */
    List<TeamJoinRequest> findByTeamAndStatus(Team team, JoinRequestStatusForOverAllTeam status);

    /**
     * Count requests for a team by status.
     */
    int countByTeamAndStatus(Team team, JoinRequestStatusForOverAllTeam status);

    /**
     * Check if a specific player already has a request in a given status.
     */
    Optional<TeamJoinRequest> findByTeamAndPlayerAndStatus(
            Team team,
            User player,
            JoinRequestStatus status
    );

    /**
     * Fetch request history excluding a particular status (e.g., not PENDING).
     */
    List<TeamJoinRequest> findByTeamAndStatusNot(
            Team team,
            JoinRequestStatusForOverAllTeam status
    );

    /**
     * Count new requests created after a certain time.
     */
    @Query("""
        SELECT COUNT(r) FROM TeamJoinRequest r
        WHERE r.team = :team
          AND r.status = :status
          AND r.createdAt >= :since
    """)
    int countNewSince(
            @Param("team") Team team,
            @Param("status") JoinRequestStatusForOverAllTeam status,
            @Param("since") LocalDateTime since
    );

    /**
     * Count requests expiring within a time window.
     */
    @Query("""
        SELECT COUNT(r) FROM TeamJoinRequest r
        WHERE r.team = :team
          AND r.status = :status
          AND r.expiresAt BETWEEN :now AND :soon
    """)
    int countExpiringBetween(
            @Param("team") Team team,
            @Param("status") JoinRequestStatusForOverAllTeam status,
            @Param("now") LocalDateTime now,
            @Param("soon") LocalDateTime soon
    );
}
