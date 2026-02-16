package in.crewplay.crewplay_backend.domain.match.controller;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.dto.SubmitSquadRequest;
import in.crewplay.crewplay_backend.domain.match.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    /**
     * 🔥 SINGLE ENTRY POINT — PHASE-0
     * Called ONLY when scorer clicks "Start Match"
     *
     * Flow:
     * UI navigation (Quick Match, Select Teams, Select Squads) → frontend only
     * Final "Proceed" button → THIS API
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('SCORER')")
    public ResponseEntity<Match> startMatch(
            @RequestAttribute("userId") Long scorerUserId,
            @Valid @RequestBody SubmitSquadRequest request
    ) {
        Match match = matchService.startMatch(scorerUserId, request);
        return ResponseEntity.ok(match);
    }
}
