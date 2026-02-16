package in.crewplay.crewplay_backend.domain.match.controller;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.dto.MatchSpecsRequest;
import in.crewplay.crewplay_backend.domain.match.service.MatchSpecsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/match/specs")
@RequiredArgsConstructor
public class MatchSpecsController {

    private final MatchSpecsService matchSpecsService;

    @PreAuthorize("hasRole('SCORER')")
    @PostMapping("/{matchId}")
    public ResponseEntity<Match> applySpecs(
            @RequestAttribute("userId") Long scorerUserId,
            @PathVariable Long matchId,
            @Valid @RequestBody MatchSpecsRequest request
    ) {
        Match match = matchSpecsService.applySpecs(scorerUserId, matchId, request);
        return ResponseEntity.ok(match);
    }
}
