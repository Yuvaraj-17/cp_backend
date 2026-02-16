package in.crewplay.crewplay_backend.domain.match.innings.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InningsContextResponse {

    private InningsTeamDTO battingTeam;
    private InningsTeamDTO bowlingTeam;
}

