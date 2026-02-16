package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Used for detailed player profile analytics.
 */
@Getter
@Builder
public class PlayerProfileStatsResponse {

    private Long userId;
    private Integer totalRuns;
}
