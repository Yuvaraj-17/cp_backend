package in.crewplay.crewplay_backend.domain.match.scoring.repository;

import in.crewplay.crewplay_backend.domain.match.innings.MatchInnings;
import in.crewplay.crewplay_backend.domain.match.scoring.entity.FallOfWicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FallOfWicketRepository
        extends JpaRepository<FallOfWicket, Long> {

    List<FallOfWicket> findByInningsOrderByWicketNumberAsc(
            MatchInnings innings
    );
}