package in.crewplay.crewplay_backend.team.repository;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.teams.PlayerAvailability;
import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.domain.teams.enums.AvailabilityStatus;
import in.crewplay.crewplay_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerAvailabilityRepository
        extends JpaRepository<PlayerAvailability, Long> {

    // Load full squad state
    List<PlayerAvailability> findByMatchAndTeam(Match match, Team team);

    // Find specific player availability
    Optional<PlayerAvailability> findByMatchAndTeamAndPlayer(
            Match match,
            Team team,
            User player
    );

    // Filter by availability status
    List<PlayerAvailability> findByMatchAndTeamAndStatus(
            Match match,
            Team team,
            AvailabilityStatus status
    );

    // Selected Playing XI
    List<PlayerAvailability> findByMatchAndTeamAndIsInPlayingXiTrue(
            Match match,
            Team team
    );

    int countByMatchAndTeamAndIsInPlayingXiTrue(
            Match match,
            Team team
    );

    // Count by status
    int countByMatchAndTeamAndStatus(
            Match match,
            Team team,
            AvailabilityStatus status
    );

    // Captain lookup
    Optional<PlayerAvailability> findByMatchAndTeamAndIsCaptainTrue(
            Match match,
            Team team
    );

    // Wicketkeeper lookup
    Optional<PlayerAvailability> findByMatchAndTeamAndIsWicketKeeperTrue(
            Match match,
            Team team
    );
}
