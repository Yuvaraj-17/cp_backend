package in.crewplay.crewplay_backend.team_manager.Controller;

import in.crewplay.crewplay_backend.team_manager.Service.ManagerAnalyticsService;
import in.crewplay.crewplay_backend.team_manager.dto.response.FullTeamStatsResponse;
import in.crewplay.crewplay_backend.team_manager.dto.response.MatchAnalyticsResponse;
import in.crewplay.crewplay_backend.team_manager.dto.response.PlayerProfileStatsResponse;
import in.crewplay.crewplay_backend.team_manager.dto.response.TeamAnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/team-manager/teams/{teamId}/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEAM_MANAGER')")
public class ManagerAnalyticsController {

    private final ManagerAnalyticsService analyticsService;

    /**
     * Team analytics (last 5 matches).
     */
    @GetMapping
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<TeamAnalyticsResponse> getTeamAnalytics(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId) {

        return ResponseEntity.ok(
                analyticsService.getTeamAnalytics(userId, teamId)
        );
    }

    /**
     * Match analytics.
     */
    @GetMapping("/match/{matchId}")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<MatchAnalyticsResponse> getMatchAnalytics(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId,
            @PathVariable Long matchId) {

        return ResponseEntity.ok(
                analyticsService.getMatchAnalytics(userId, teamId, matchId)
        );
    }

    /**
     * Full team stats.
     */
    @GetMapping("/full-stats")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<FullTeamStatsResponse> getFullTeamStats(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId) {

        return ResponseEntity.ok(
                analyticsService.getFullTeamStats(userId, teamId)
        );
    }

    /**
     * Individual player stats.
     */
    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<PlayerProfileStatsResponse> getPlayerStats(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId,
            @PathVariable Long playerId) {

        return ResponseEntity.ok(
                analyticsService.getPlayerStats(userId, teamId, playerId)
        );
    }
}
