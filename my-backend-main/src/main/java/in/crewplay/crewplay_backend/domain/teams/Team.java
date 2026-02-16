package in.crewplay.crewplay_backend.domain.teams;

import in.crewplay.crewplay_backend.domain.teams.enums.TeamStatus;
import in.crewplay.crewplay_backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "teams",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "team_code"),
                @UniqueConstraint(columnNames = {"name", "city"})
        }
)
@Getter
@Setter
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------- BASIC INFO ----------

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(name = "logo_url")
    private String logoUrl;

    // ---------- TEAM CODE (JOINING / QR / LINK) ----------

    @Column(name = "team_code", nullable = false, unique = true)
    private String teamCode;

    // ---------- CAPTAIN & OWNERSHIP ----------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captain_user_id", nullable = false)
    private User captain;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamStatus status = TeamStatus.TEMPORARY;

    // ---------- TRUST / VERIFICATION ----------

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false; // Phase-0 always false

    @Column(name = "verification_method")
    private String verificationMethod; // EMAIL (Phase-0)

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // ---------- SYSTEM ----------

    @Column(name = "created_by_scorer_id", nullable = false)
    private Long createdByScorerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // These are the attributes for the team manager:
    @Column(name = "manager_user_id")
    private Long managerUserId;   // ← Set when manager creates/claims team

    @Column(name = "home_ground")
    private String homeGround;    // ← Shown in match cards

    @Column(name = "active_league")
    private String activeLeague;  // ← e.g. "Premier League 2024"

    @Column(name = "wins", nullable = false)
    private Integer wins = 0;

    @Column(name = "losses", nullable = false)
    private Integer losses = 0;

    @Column(name = "draws", nullable = false)
    private Integer draws = 0;
}
