package in.crewplay.crewplay_backend.team_manager.Controller;

import in.crewplay.crewplay_backend.domain.teams.TeamMember;
import in.crewplay.crewplay_backend.team_manager.dto.request.AddGuestRequest;
import in.crewplay.crewplay_backend.team_manager.dto.request.InvitePlayerRequest;
import in.crewplay.crewplay_backend.team_manager.dto.response.RosterPlayerResponse;
import in.crewplay.crewplay_backend.team_manager.Service.ManagerRosterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles team roster management.
 */
@RestController
@RequestMapping("/team-manager/teams/{teamId}/roster")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEAM_MANAGER')")
public class ManagerRosterController {

    private final ManagerRosterService rosterService;

    @GetMapping
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<List<RosterPlayerResponse>> getRoster(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId) {

        return ResponseEntity.ok(
                rosterService.getRoster(userId, teamId)
        );
    }

    @PostMapping("/invite")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<String> invitePlayer(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId,
            @RequestBody InvitePlayerRequest request) {

        return ResponseEntity.ok(
                rosterService.invitePlayerByMobile(userId, teamId, request)
        );
    }

    @PostMapping("/guest")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<TeamMember> addGuest(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId,
            @RequestBody AddGuestRequest request) {

        return ResponseEntity.ok(
                rosterService.addGuestPlayer(userId, teamId, request)
        );
    }

    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<Void> removePlayer(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId,
            @PathVariable Long memberId) {

        rosterService.removePlayer(userId, teamId, memberId);
        return ResponseEntity.noContent().build();
    }
}
