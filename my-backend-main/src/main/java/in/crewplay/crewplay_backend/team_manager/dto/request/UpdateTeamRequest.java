package in.crewplay.crewplay_backend.team_manager.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Used to update team details.
 */
@Getter
@Setter
public class UpdateTeamRequest {

    private String teamName;
    private String logoUrl;
    private String homeGround;
    private String activeLeague;
}
