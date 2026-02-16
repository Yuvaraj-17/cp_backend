package in.crewplay.crewplay_backend.domain.match.innings.controller;

import in.crewplay.crewplay_backend.domain.match.innings.dto.InningsContextResponse;
import in.crewplay.crewplay_backend.domain.match.innings.dto.StartInningsRequest;
import in.crewplay.crewplay_backend.domain.match.innings.service.InningsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class InningsController {

    private final InningsService inningsService;

    @GetMapping("/{matchId}/innings/context")
    @PreAuthorize("hasRole('SCORER')")
    public ResponseEntity<InningsContextResponse> getContext(
            @PathVariable Long matchId
    ) {
        return ResponseEntity.ok(
                inningsService.getInningsContext(matchId)
        );
    }

    @PostMapping("/{matchId}/innings/start")
    @PreAuthorize("hasRole('SCORER')")
    public ResponseEntity<?> startInnings(
            @PathVariable Long matchId,
            @RequestAttribute("userId") Long scorerUserId,
            @Valid @RequestBody StartInningsRequest request
    ) {
        inningsService.startInnings(matchId, scorerUserId, request);
        return ResponseEntity.ok("Innings started");
    }
}