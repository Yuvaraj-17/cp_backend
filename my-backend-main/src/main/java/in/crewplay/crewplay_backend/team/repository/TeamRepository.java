package in.crewplay.crewplay_backend.team.repository;

import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.domain.teams.enums.TeamStatus;
import in.crewplay.crewplay_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByTeamCode(String teamCode);

    List<Team> findByCreatedByScorerId(Long scorerId);

    boolean existsByNameAndCity(String name, String city);

    List<Team> findByCaptainAndStatus(User captain, TeamStatus status);

    List<Team> findByManagerUserId(Long managerUserId);
    List<Team> findByCaptain(User captain);

}
