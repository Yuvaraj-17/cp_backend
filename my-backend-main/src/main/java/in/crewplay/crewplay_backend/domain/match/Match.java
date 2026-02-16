package in.crewplay.crewplay_backend.domain.match;

import in.crewplay.crewplay_backend.domain.teams.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Getter
@Setter
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scorer_user_id", nullable = false)
    private Long scorerUserId;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.DRAFT;

    private String city;

    // 🔹 MATCH SPECS
    @Enumerated(EnumType.STRING)
    @Column(name = "match_type")
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ball_type")
    private BallType ballType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pitch_type")
    private PitchType pitchType;

    private Integer overs;

    // 🔹 PARTICIPATING TEAMS
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_id", nullable = false)
    private Team teamA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_id", nullable = false)
    private Team teamB;

    // 🔹 TOSS INFO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toss_winner_team_id")
    private Team tossWinnerTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "toss_decision")
    private TossDecision tossDecision;

    // 🔹 LIVE GAME STATE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batting_team_id")
    private Team battingTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowling_team_id")
    private Team bowlingTeam;

    // 🔹 WINNER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    // ───────────── Helper Methods ─────────────

    public boolean containsTeam(Long teamId) {
        if (teamId == null) return false;

        return (teamA != null && teamA.getId().equals(teamId)) ||
                (teamB != null && teamB.getId().equals(teamId));
    }

    public boolean isCompleted() {
        return status == MatchStatus.COMPLETED;
    }

    public boolean isLive() {
        return status == MatchStatus.LIVE;
    }

    public Long getTeamAId() {
        return teamA != null ? teamA.getId() : null;
    }

    public Long getTeamBId() {
        return teamB != null ? teamB.getId() : null;
    }

    public Long getWinnerTeamId() {
        return winnerTeam != null ? winnerTeam.getId() : null;
    }
}
