package in.crewplay.crewplay_backend.domain.teams;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.teams.enums.AvailabilityStatus;
import in.crewplay.crewplay_backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Stores per-match squad state of a player.
 *
 * This entity represents match-scoped selection data:
 * - Availability (AVAILABLE / UNAVAILABLE / etc.)
 * - Playing XI inclusion
 * - Captain / Wicketkeeper role
 *
 * One record per (match + team + player).
 * Enforced via unique constraint.
 */
@Entity
@Table(
        name = "player_availability",

        // Prevent duplicate availability records for same match/team/player
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"match_id", "team_id", "player_user_id"}
        ),

        // Indexes for squad queries and performance
        indexes = {
                @Index(name = "idx_pa_match_team", columnList = "match_id, team_id"),
                @Index(name = "idx_pa_player", columnList = "player_user_id")
        }
)
@Getter
@Setter
public class PlayerAvailability {

    /**
     * Primary key (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Match to which this availability belongs.
     * Mandatory relationship.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    /**
     * Team context for the match.
     * Mandatory relationship.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /**
     * Player whose availability is being tracked.
     * Mandatory relationship.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_user_id", nullable = false)
    private User player;

    /**
     * Player's availability status for this match.
     * Default: PENDING until player confirms.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus status = AvailabilityStatus.PENDING;

    /**
     * True if player is selected in final Playing XI.
     * Controlled by manager during squad confirmation.
     */
    @Column(name = "is_in_playing_xi", nullable = false)
    private Boolean isInPlayingXi = false;

    /**
     * True if player is assigned as Captain for this match.
     * Exactly one captain should exist per team per match
     * (validated at service layer).
     */
    @Column(name = "is_captain", nullable = false)
    private Boolean isCaptain = false;

    /**
     * True if player is assigned as Wicketkeeper for this match.
     * Exactly one wicketkeeper should exist per team per match
     * (validated at service layer).
     */
    @Column(name = "is_wicket_keeper", nullable = false)
    private Boolean isWicketKeeper = false;

    /**
     * Timestamp when player confirmed availability.
     * Null until player responds.
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * Creation timestamp of availability record.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
