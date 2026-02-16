package in.crewplay.crewplay_backend.domain.match.innings.repository;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.innings.MatchInnings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchInningsRepository
        extends JpaRepository<MatchInnings, Long> {

    Optional<MatchInnings> findByMatch(Match match);
}
