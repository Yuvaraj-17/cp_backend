package in.crewplay.crewplay_backend.domain.teams;

import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.common.enums.PlayerAddMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "team_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"team_id", "user_id"})
        }
)
@Getter
@Setter
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------- RELATIONS ----------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /**
     * Can be NULL for guest players
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // ---------- PLAYER INFO ----------

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    @Column(name = "is_guest", nullable = false)
    private Boolean isGuest = false;

    // ---------- HOW THEY WERE ADDED ----------

    @Enumerated(EnumType.STRING)
    @Column(name = "add_method", nullable = false)
    private PlayerAddMethod addMethod;

    @Column(name = "added_by_scorer_id", nullable = false)
    private Long addedByScorerId;

    // ---------- TRUST / REPORTING (PHASE-1 READY) ----------

    @Column(name = "reported", nullable = false)
    private Boolean reported = false;

    // ---------- SYSTEM ----------

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
