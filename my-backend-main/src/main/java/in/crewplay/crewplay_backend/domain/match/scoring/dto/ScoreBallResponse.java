package in.crewplay.crewplay_backend.domain.match.scoring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoreBallResponse {
    private LiveScoreResponse liveScore;

    private EndOverResponse endOverResponse;
}
