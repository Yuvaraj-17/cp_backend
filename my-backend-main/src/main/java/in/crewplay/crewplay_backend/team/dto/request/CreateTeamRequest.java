package in.crewplay.crewplay_backend.team.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTeamRequest {

    private String teamName;
    private String city;
    private String logoUrl;
    private String captainMobileNumber;
}
