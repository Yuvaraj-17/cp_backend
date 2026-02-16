package in.crewplay.crewplay_backend.domain.match.innings.dto;

import in.crewplay.crewplay_backend.domain.match.BattingHand;
import in.crewplay.crewplay_backend.domain.match.BowlingType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartInningsRequest {

    @NotNull
    private Long strikerUserId;

    @NotNull
    private Long nonStrikerUserId;

    @NotNull
    private BattingHand strikerHand;

    @NotNull
    private BattingHand nonStrikerHand;

    @NotNull
    private Long bowlerUserId;

    @NotNull
    private BowlingType bowlingType;


}
