package in.crewplay.crewplay_backend.domain.match.scoring.dto;

import in.crewplay.crewplay_backend.domain.match.BattingHand;
import in.crewplay.crewplay_backend.domain.match.BowlingType;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.BallResultType;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.ExtraType;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.WicketType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoreBallRequest {

    @NotNull
    private Long matchId;

    @NotNull
    private Long strikerId;

    @NotNull
    private Long bowlerId;

    // ⬇️ ADD THESE FIELDS TO FIX THE ERRORS ⬇️
    private BattingHand strikerHand;
    private BattingHand nonStrikerHand;
    private BowlingType bowlingType;

    @NotNull
    private BallResultType ballResultType;

    private int runsOffBat = 0;

    private int extraRuns = 0;

    private ExtraType extraType;

    private boolean wicket = false; // Note: Lombok generates 'isWicket()' for booleans

    private WicketType wicketType;



    private WicketRequest wicketDetails;

    private Long dismissedPlayerId;
}