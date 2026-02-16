package in.crewplay.crewplay_backend.team_manager.Controller;

import in.crewplay.crewplay_backend.team_manager.dto.request.ConfirmPlayingXiRequest;
import in.crewplay.crewplay_backend.team_manager.dto.request.UpdateAvailabilityRequest;
import in.crewplay.crewplay_backend.team_manager.dto.response.SquadManagementResponse;
import in.crewplay.crewplay_backend.team_manager.Service.ManagerSquadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Handles squad and Playing XI management.
 */
@RestController
@RequestMapping("/team-manager/teams/{teamId}/squad")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEAM_MANAGER')")
public class ManagerSquadController {

    private final ManagerSquadService squadService;

    /**
     * Get full squad view for match.
     */
    @GetMapping("/{matchId}")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<SquadManagementResponse> getSquadView(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId,
            @PathVariable Long matchId) {

        return ResponseEntity.ok(
                squadService.getSquadView(userId, teamId, matchId)
        );
    }

    /**
     * Player updates their availability.
     */
    @PostMapping("/availability")
    @PreAuthorize("hasAnyRole('PLAYER','TEAM_MANAGER')")
    public ResponseEntity<Void> updateAvailability(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId,
            @RequestBody UpdateAvailabilityRequest request) {

        squadService.updatePlayerAvailability(userId, teamId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Manager confirms Playing XI.
     */
    @PostMapping("/confirm-xi")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<Void> confirmPlayingXi(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId,
            @RequestBody ConfirmPlayingXiRequest request) {

        squadService.confirmPlayingXi(userId, teamId, request);
        return ResponseEntity.ok().build();
    }
}
