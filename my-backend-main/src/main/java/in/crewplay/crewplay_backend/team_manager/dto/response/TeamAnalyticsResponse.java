package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Used for Team analytics screen.
 */
@Getter
@Builder
public class TeamAnalyticsResponse {

    private Double winRate;
    private Integer avgScore;
    private Double netRunRate;

    private List<Integer> formTrend;

    private List<TopContributorResponse> topBatters;
    private List<TopContributorResponse> topBowlers;
}
