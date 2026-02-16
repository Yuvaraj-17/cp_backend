package in.crewplay.crewplay_backend.team_manager.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Used when manager creates a new team.
 */
@Getter
@Setter
public class CreateTeamManagerRequest {

    private String teamName;
    private String city;
    private String logoUrl;
    private String homeGround;

    private boolean makeManagerCaptain;   // true → manager becomes captain
    private String captainMobileNumber;   // required if not self-captain
}
