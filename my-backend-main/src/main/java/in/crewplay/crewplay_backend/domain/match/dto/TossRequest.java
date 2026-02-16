package in.crewplay.crewplay_backend.domain.match.dto;

import in.crewplay.crewplay_backend.domain.match.TossDecision;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossRequest {

    @NotNull
    private Long tossWinnerTeamId;

    @NotNull
    private TossDecision decision;
}
