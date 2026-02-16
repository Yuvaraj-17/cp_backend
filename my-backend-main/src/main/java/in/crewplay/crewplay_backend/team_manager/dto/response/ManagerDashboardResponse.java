package in.crewplay.crewplay_backend.team_manager.dto.response;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ManagerDashboardResponse {
    private UpcomingMatchCard upcomingMatch;
    private int pendingApprovalsCount;
    private List<LeagueUpdateResponse> leagueUpdates;

}
