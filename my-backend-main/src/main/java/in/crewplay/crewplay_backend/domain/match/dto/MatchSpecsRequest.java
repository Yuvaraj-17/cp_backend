package in.crewplay.crewplay_backend.domain.match.dto;

import in.crewplay.crewplay_backend.domain.match.BallType;
import in.crewplay.crewplay_backend.domain.match.MatchType;
import in.crewplay.crewplay_backend.domain.match.PitchType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchSpecsRequest {

    @NotNull
    private MatchType matchType;

    @NotNull
    private BallType ballType;

    @NotNull
    private PitchType pitchType;

    @NotNull
    @Positive
    private Integer overs;

    private String city; // optional override
}
