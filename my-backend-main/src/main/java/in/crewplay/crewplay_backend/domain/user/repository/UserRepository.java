package in.crewplay.crewplay_backend.domain.user.repository; // Ensure this matches your folder

import in.crewplay.crewplay_backend.domain.user.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // This allows .findByEmail() to work in AuthController
    Optional<User> findByEmail(String email);
    Optional<User> findByMobileNumber(String mobileNumber);

    /**
     * Search active users by email (case-insensitive),
     * excluding current user and already-associated users.
     *
     * Used for Recruit / Discover players screen.
     */
    @Query("""
    SELECT u FROM User u
    WHERE u.isActive = true
      AND u.id != :excludeId
      AND (:excludeIds IS NULL OR u.id NOT IN :excludeIds)
      AND LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
""")
    List<User> searchByEmail(
            @Param("query") String query,
            @Param("excludeId") Long excludeId,
            @Param("excludeIds") List<Long> excludeIds
    );


    /**
     * Fetch all active users excluding specific IDs.
     * Used when no search query is provided.
     */
    @Query("""
    SELECT u FROM User u
    WHERE u.isActive = true
      AND u.id != :excludeId
      AND (:excludeIds IS NULL OR u.id NOT IN :excludeIds)
""")
    List<User> findAllExcluding(
            @Param("excludeId") Long excludeId,
            @Param("excludeIds") List<Long> excludeIds
    );



}