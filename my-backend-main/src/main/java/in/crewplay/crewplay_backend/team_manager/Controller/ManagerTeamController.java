package in.crewplay.crewplay_backend.team_manager.Controller;

import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.team_manager.dto.request.CreateTeamManagerRequest;
import in.crewplay.crewplay_backend.team_manager.dto.request.UpdateTeamRequest;
import in.crewplay.crewplay_backend.team_manager.dto.response.TeamSummaryResponse;
import in.crewplay.crewplay_backend.team_manager.Service.ManagerTeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles team creation and management.
 */
@RestController
@RequestMapping("/team-manager/teams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEAM_MANAGER')")
public class ManagerTeamController {

    private final ManagerTeamService teamService;

    /**
     * Create a new team (Manager Mode).
     */
    @PostMapping
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<Team> createTeam(
            @RequestAttribute("userId") Long userId,
            @RequestBody CreateTeamManagerRequest request) {

        return ResponseEntity.ok(
                teamService.createTeam(userId, request)
        );
    }

    /**
     * Get all teams managed by user.
     */
    @GetMapping
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<List<TeamSummaryResponse>> getMyTeams(
            @RequestAttribute("userId") Long userId) {

        return ResponseEntity.ok(
                teamService.getMyTeams(userId)
        );
    }

    /**
     * Switch active team.
     */
    @PostMapping("/{teamId}/switch")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<Void> switchTeam(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId) {

        teamService.switchActiveTeam(userId, teamId);
        return ResponseEntity.ok().build();
    }

    /**
     * Update team details.
     */
    @PutMapping("/{teamId}")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<Team> updateTeam(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId,
            @RequestBody UpdateTeamRequest request) {

        return ResponseEntity.ok(
                teamService.updateTeam(userId, teamId, request)
        );
    }
}
