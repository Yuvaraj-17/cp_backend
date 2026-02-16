package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Used for full team statistics table.
 */
@Getter
@Builder
public class FullTeamStatsResponse {

    private List<PlayerStatRow> players;
}
