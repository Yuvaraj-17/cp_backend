package in.crewplay.crewplay_backend.domain.match.service;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.MatchStatus;
import in.crewplay.crewplay_backend.domain.match.dto.MatchSpecsRequest;
import in.crewplay.crewplay_backend.domain.match.repository.MatchRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchSpecsService {

    private final MatchRepository matchRepository;

    @Transactional
    public Match applySpecs(Long scorerUserId, Long matchId, MatchSpecsRequest request) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalStateException("Match not found"));

        // 🔐 Ensure only creator scorer can modify
        if (!match.getScorerUserId().equals(scorerUserId)) {
            throw new IllegalStateException("You are not allowed to modify this match");
        }

        // 🔒 Allow specs only if match is in READY or DRAFT
        if (match.getStatus() != MatchStatus.READY &&
                match.getStatus() != MatchStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify specs after match started");
        }

        // ✅ Apply specs
        match.setMatchType(request.getMatchType());
        match.setBallType(request.getBallType());
        match.setPitchType(request.getPitchType());
        match.setOvers(request.getOvers());

        if (request.getCity() != null && !request.getCity().isBlank()) {
            match.setCity(request.getCity());
        }

        // 🔄 Move status forward
        match.setStatus(MatchStatus.SPECS_LOCKED);

        return matchRepository.save(match);
    }
}
