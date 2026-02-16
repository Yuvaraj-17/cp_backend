package in.crewplay.crewplay_backend.domain.match.scoring.entity;

import in.crewplay.crewplay_backend.domain.match.scoring.enums.BallResultType;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.WicketType;
import in.crewplay.crewplay_backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "wicket_details")
@Getter
@Setter
public class WicketDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ball_event_id")
    private BallEvent ballEvent;

    @Enumerated(EnumType.STRING)
    private WicketType wicketType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dismissed_player_id")
    private User dismissedPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fielder_id")
    private User fielder; // For caught/run-out

    private Boolean directHit; // For run-out

    private Integer runsCompleted; // Runs completed before run-out

    @Enumerated(EnumType.STRING)
    private BallResultType deliveryType; // NORMAL, WIDE, NO_BALL etc
}