package in.crewplay.crewplay_backend.domain.match.controller;

import in.crewplay.crewplay_backend.domain.match.dto.AssignSquadRolesRequest;
import in.crewplay.crewplay_backend.domain.match.service.MatchRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchRoleController {

    private final MatchRoleService matchRoleService;

    @PreAuthorize("hasRole('SCORER')")
    @PostMapping("/{matchId}/assign-roles")
    public ResponseEntity<?> assignRoles(
            @PathVariable Long matchId,
            @Valid @RequestBody AssignSquadRolesRequest request
    ) {
        matchRoleService.assignRoles(matchId, request);
        return ResponseEntity.ok().build();
    }
}
