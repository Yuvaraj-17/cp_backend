package in.crewplay.crewplay_backend.domain.match.verification.repository;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.MatchTeam;
import in.crewplay.crewplay_backend.domain.match.verification.MatchVerification;
import in.crewplay.crewplay_backend.domain.match.verification.MatchVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchVerificationRepository extends JpaRepository<MatchVerification, Long> {

    Optional<MatchVerification> findByMatchAndMatchTeam(Match match, MatchTeam matchTeam);

    List<MatchVerification> findByMatch(Match match);

    boolean existsByMatchAndStatus(Match match, MatchVerificationStatus status);
}
