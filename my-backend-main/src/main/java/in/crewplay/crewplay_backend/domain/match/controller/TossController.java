package in.crewplay.crewplay_backend.domain.match.controller;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.dto.TossRequest;
import in.crewplay.crewplay_backend.domain.match.service.TossService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/match/toss")
@RequiredArgsConstructor
public class TossController {

    private final TossService tossService;

    @PreAuthorize("hasRole('SCORER')")
    @PostMapping("/{matchId}")
    public ResponseEntity<Match> applyToss(
            @RequestAttribute("userId") Long scorerUserId,
            @PathVariable Long matchId,
            @Valid @RequestBody TossRequest request
    ) {
        Match match = tossService.applyToss(scorerUserId, matchId, request);
        return ResponseEntity.ok(match);
    }
}
