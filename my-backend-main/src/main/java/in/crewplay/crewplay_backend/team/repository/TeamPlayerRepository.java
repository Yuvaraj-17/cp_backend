package in.crewplay.crewplay_backend.team.repository;

import in.crewplay.crewplay_backend.domain.teams.TeamPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, Long> {

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    List<TeamPlayer> findByTeamIdAndIsActiveTrue(Long teamId);
}