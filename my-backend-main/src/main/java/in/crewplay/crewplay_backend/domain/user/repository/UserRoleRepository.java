package in.crewplay.crewplay_backend.domain.user.repository;

import in.crewplay.crewplay_backend.domain.user.UserRole;
import in.crewplay.crewplay_backend.domain.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    Optional<UserRole> findByUserIdAndRole(Long userId, Role role);

    Optional<UserRole> findByUserIdAndIsActiveTrue(Long userId);


}