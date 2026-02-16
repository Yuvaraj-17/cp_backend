package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Shown in "My Teams" screen.
 */
@Getter
@Builder
public class TeamSummaryResponse {

    private Long teamId;
    private String teamName;
    private String logoUrl;
    private String homeGround;
    private String activeLeague;

    private String status;

    private Integer wins;
    private Integer losses;
    private Integer draws;

    private Boolean isCurrentActive;
    private String teamCode;

    private Integer totalPlayers;
}
