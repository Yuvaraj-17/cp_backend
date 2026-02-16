package in.crewplay.crewplay_backend.domain.match.repository;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.MatchTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchTeamRepository extends JpaRepository<MatchTeam, Long> {
    List<MatchTeam> findByMatch(Match match);
    List<MatchTeam> findByMatch_Id(Long matchId);

    Optional<MatchTeam> findByMatch_IdAndTeam_Id(Long matchId, Long teamId);
}
