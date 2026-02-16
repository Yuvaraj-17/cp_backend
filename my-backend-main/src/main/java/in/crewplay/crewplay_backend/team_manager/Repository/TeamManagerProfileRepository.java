package in.crewplay.crewplay_backend.team_manager.Repository;

import in.crewplay.crewplay_backend.domain.user.TeamManagerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamManagerProfileRepository
        extends JpaRepository<TeamManagerProfile, Long> {

    // Find profile by underlying User id (from JWT)
    Optional<TeamManagerProfile> findByUser_Id(Long userId);
}