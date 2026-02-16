package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Used for individual match analytics.
 */
@Getter
@Builder
public class MatchAnalyticsResponse {

    private Long matchId;

    private Integer teamScore;
    private Integer opponentScore;

    private String result;
}
