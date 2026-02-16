package in.crewplay.crewplay_backend.Scorer.Repository;

import in.crewplay.crewplay_backend.domain.user.ScorerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScorerProfileRepository extends JpaRepository<ScorerProfile, Long> {
}
