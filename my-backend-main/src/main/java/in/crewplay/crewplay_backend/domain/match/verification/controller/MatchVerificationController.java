package in.crewplay.crewplay_backend.domain.match.verification.controller;

import in.crewplay.crewplay_backend.domain.match.verification.dto.VerificationContextResponse;
import in.crewplay.crewplay_backend.domain.match.verification.service.MatchVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchVerificationController {

    private final MatchVerificationService verificationService;

    @GetMapping("/{matchId}/verification/context")
    @PreAuthorize("hasRole('SCORER')")
    public ResponseEntity<VerificationContextResponse> getContext(
            @PathVariable Long matchId,
            @RequestAttribute("userId") Long scorerUserId
    ) {

        return ResponseEntity.ok(
                verificationService.getVerificationContext(matchId, scorerUserId)
        );
    }

    @PostMapping("/{matchId}/verification/send")
    @PreAuthorize("hasRole('SCORER')")
    public ResponseEntity<?> sendVerification(
            @PathVariable Long matchId,
            @RequestParam Long matchTeamId,
            @RequestParam Long selectedUserId,
            @RequestAttribute("userId") Long scorerUserId
    ) {
        verificationService.sendVerificationRequest(
                scorerUserId,
                matchId,
                matchTeamId,
                selectedUserId
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{matchId}/verification/accept")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> accept(
            @PathVariable Long matchId,
            @RequestAttribute("userId") Long userId
    ) {
        verificationService.acceptVerification(matchId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{matchId}/verification/reject")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> reject(
            @PathVariable Long matchId,
            @RequestAttribute("userId") Long userId
    ) {
        verificationService.rejectVerification(matchId, userId);
        return ResponseEntity.ok().build();
    }


}