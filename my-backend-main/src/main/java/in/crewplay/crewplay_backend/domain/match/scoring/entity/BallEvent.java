package in.crewplay.crewplay_backend.domain.match.scoring.entity;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.innings.MatchInnings;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.BallResultType;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.ExtraType;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.WicketType;
import in.crewplay.crewplay_backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Entity
@Table(name = "ball_events")
@Getter
@Setter
public class BallEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innings_id", nullable = false)
    private MatchInnings innings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowler_id", nullable = false)
    private User bowler;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batsman_id", nullable = false)
    private User batsman;

    // Over tracking
    private int overNumber;
    private int ballNumber; // only for valid deliveries

    // Core ball classification
    @Enumerated(EnumType.STRING)
    private BallResultType ballResultType;

    // Runs
    private int runsOffBat = 0;
    private int extraRuns = 0;

    // Extras classification
    @Enumerated(EnumType.STRING)
    private ExtraType extraType;

    private boolean isUndone = false;

    // Wicket
    private boolean isWicket = false;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dismissed_player_id")
    private User dismissedPlayer;

    // For CAUGHT / RUN OUT / STUMPED
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fielder_id")
    private User fielder;


    @Enumerated(EnumType.STRING)
    private WicketType wicketType;


    // For RUN OUT
    private Boolean isDirectHit;
    private Integer runsCompletedBeforeWicket;

    // Store delivery classification at dismissal time
    @Enumerated(EnumType.STRING)
    private BallResultType dismissalDeliveryType;

    private LocalDateTime createdAt = LocalDateTime.now();
}
