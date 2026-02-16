package in.crewplay.crewplay_backend.domain.match.scoring.entity;

import in.crewplay.crewplay_backend.domain.match.innings.MatchInnings;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.WicketType;
import in.crewplay.crewplay_backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fall_of_wickets")
@Getter
@Setter
public class FallOfWicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innings_id")
    private MatchInnings innings;

    private int wicketNumber;

    private int teamScoreAtFall;

    private int overNumber;
    private int ballNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dismissed_player_id")
    private User dismissedPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowler_id")
    private User bowler;

    @Enumerated(EnumType.STRING)
    private WicketType wicketType;
}
