package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Used for top batters/bowlers lists.
 */
@Getter
@Builder
public class TopContributorResponse {

    private Long userId;
    private Integer runs;
    private Integer balls;
    private Integer wickets;
    private Double strikeRate;
    private Double economy;
}
