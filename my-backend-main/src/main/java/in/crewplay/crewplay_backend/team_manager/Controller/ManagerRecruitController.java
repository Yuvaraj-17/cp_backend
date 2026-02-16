package in.crewplay.crewplay_backend.team_manager.Controller;

import in.crewplay.crewplay_backend.team_manager.Service.ManagerRecruitService;
import in.crewplay.crewplay_backend.team_manager.dto.response.DiscoverPlayerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/team-manager/recruit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEAM_MANAGER')")
public class ManagerRecruitController {

    private final ManagerRecruitService recruitService;

    @GetMapping("/discover")
    public ResponseEntity<List<DiscoverPlayerResponse>> discoverPlayers(
            @RequestAttribute("userId") Long userId,
            @RequestParam Long teamId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "ALL") String role) {

        return ResponseEntity.ok(
                recruitService.discoverPlayers(userId, teamId, query, role)
        );
    }

    @PostMapping("/{playerId}/invite")
    public ResponseEntity<Map<String, String>> sendInvite(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long playerId,
            @RequestParam Long teamId) {

        recruitService.sendInvite(userId, teamId, playerId);

        return ResponseEntity.ok(Map.of(
                "status", "INVITED",
                "message", "Invite sent. The player has 7 days to accept."
        ));
    }
}
