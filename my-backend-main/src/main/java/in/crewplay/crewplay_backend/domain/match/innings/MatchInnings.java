package in.crewplay.crewplay_backend.domain.match.innings;

import in.crewplay.crewplay_backend.domain.match.BattingHand;
import in.crewplay.crewplay_backend.domain.match.BowlingType;
import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_innings")
@Getter
@Setter
public class MatchInnings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    private int inningsNumber; // 1 or 2

    // 🔢 Score state
    private int totalRuns = 0;
    private int totalWickets = 0;

    private int currentOver = 0;
    private int currentBall = 0; // 0–5 (valid balls only)

    private int maxOvers; // from match specs

    // 🏏 Batting state
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "striker_id")
    private User striker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "non_striker_id")
    private User nonStriker;

    // 🎯 Bowling state
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_bowler_id")
    private User currentBowler;

    // Match state
    private boolean completed = false;

    @Enumerated(EnumType.STRING)
    private InningsStatus status = InningsStatus.LIVE;

    private int totalValidBalls = 0;

    private Integer target; // only for 2nd innings

    private boolean freeHit = false;
    private boolean awaitingBowlerAfterBatsman = false;

    private BattingHand strikerHand;    // e.g., "Right-hand"
    private BattingHand nonStrikerHand; // e.g., "Left-hand"
    private BowlingType bowlerType;     // e.g., "Right-arm Fast"

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batting_team_id", nullable = false)
    private Team battingTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowling_team_id", nullable = false)
    private Team bowlingTeam;

}