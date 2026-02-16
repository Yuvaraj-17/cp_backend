package in.crewplay.crewplay_backend.Scorer.Controller;

import in.crewplay.crewplay_backend.Scorer.Service.ScorerService;
import in.crewplay.crewplay_backend.domain.user.ScorerProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scorer")
@RequiredArgsConstructor
public class ScorerController {

    private final ScorerService scorerService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('SCORER')")
    public ResponseEntity<ScorerProfile> getMyScorerProfile(
            @RequestAttribute("userId") Long userId
    ) {
        ScorerProfile profile = scorerService.getOrCreateScorerProfile(userId);
        return ResponseEntity.ok(profile);
    }
}
