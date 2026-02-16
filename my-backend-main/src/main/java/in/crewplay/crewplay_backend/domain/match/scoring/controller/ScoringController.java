package in.crewplay.crewplay_backend.domain.match.scoring.controller;

import in.crewplay.crewplay_backend.domain.match.scoring.dto.LiveScoreResponse;
import in.crewplay.crewplay_backend.domain.match.scoring.dto.ScoreBallRequest;
import in.crewplay.crewplay_backend.domain.match.scoring.dto.ScoreBallResponse;
import in.crewplay.crewplay_backend.domain.match.scoring.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scoring")
@RequiredArgsConstructor
public class ScoringController {

    private final ScoringService scoringService;

    @PostMapping("/ball")
    public ScoreBallResponse scoreBall(
            @RequestBody ScoreBallRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return scoringService.scoreBall(request, userId);
    }

    @GetMapping("/live/{matchId}")
    public LiveScoreResponse getLiveScore(
            @PathVariable Long matchId
    ) {
        return scoringService.getLiveScore(matchId);
    }

    @PostMapping("/replace-batsman")
    public void replaceBatsman(
            @RequestParam Long matchId,
            @RequestParam Long newBatsmanId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        scoringService.replaceBatsman(matchId, newBatsmanId, userId);
    }

    @PostMapping("/change-bowler")
    public void changeBowler(
            @RequestParam Long matchId,
            @RequestParam Long newBowlerId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        scoringService.changeBowler(matchId, newBowlerId, userId);
    }



}