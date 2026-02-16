package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeagueUpdateResponse {
    private String leagueName;
    private String status;    // "OPEN" | "ACTIVE" | "CLOSED"
    private String subtitle;
}