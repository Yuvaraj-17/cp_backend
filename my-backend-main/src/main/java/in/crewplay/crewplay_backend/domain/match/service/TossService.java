package in.crewplay.crewplay_backend.domain.match.service;

import in.crewplay.crewplay_backend.domain.match.*;
import in.crewplay.crewplay_backend.domain.match.dto.TossRequest;
import in.crewplay.crewplay_backend.domain.match.repository.MatchRepository;
import in.crewplay.crewplay_backend.domain.match.repository.MatchTeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TossService {

    private final MatchRepository matchRepository;
    private final MatchTeamRepository matchTeamRepository;

    @Transactional
    public Match applyToss(Long scorerUserId, Long matchId, TossRequest request) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalStateException("Match not found"));

        // 🔐 Ownership check
        if (!match.getScorerUserId().equals(scorerUserId)) {
            throw new IllegalStateException("Not allowed");
        }

        // 🔒 Must be VERIFIED now (NOT SPECS_LOCKED)
        if (match.getStatus() != MatchStatus.VERIFIED) {
            throw new IllegalStateException("Toss allowed only after verification");
        }

        // 🔍 Fetch match teams
        List<MatchTeam> teams = matchTeamRepository.findByMatch_Id(matchId);
        if (teams.size() != 2) {
            throw new IllegalStateException("Match teams not properly initialized");
        }

        MatchTeam teamA = teams.get(0);
        MatchTeam teamB = teams.get(1);

        boolean validTeam =
                teamA.getTeam().getId().equals(request.getTossWinnerTeamId()) ||
                        teamB.getTeam().getId().equals(request.getTossWinnerTeamId());

        if (!validTeam) {
            throw new IllegalStateException("Invalid toss winner");
        }

        // 🪙 Apply toss
        match.setTossWinnerTeamId(request.getTossWinnerTeamId());
        match.setTossDecision(request.getDecision());

        Long battingTeamId;
        Long bowlingTeamId;

        if (request.getDecision() == TossDecision.BAT) {
            battingTeamId = request.getTossWinnerTeamId();
        } else {
            battingTeamId =
                    teamA.getTeam().getId().equals(request.getTossWinnerTeamId())
                            ? teamB.getTeam().getId()
                            : teamA.getTeam().getId();
        }

        bowlingTeamId =
                teamA.getTeam().getId().equals(battingTeamId)
                        ? teamB.getTeam().getId()
                        : teamA.getTeam().getId();

        match.setBattingTeamId(battingTeamId);
        match.setBowlingTeamId(bowlingTeamId);

        // 🚦 IMPORTANT: Move to TOSS_DONE (NOT LIVE)
        match.setStatus(MatchStatus.TOSS_DONE);

        return matchRepository.save(match);
    }
}
