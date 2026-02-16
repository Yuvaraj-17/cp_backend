package in.crewplay.crewplay_backend.domain.match.innings.service;

import in.crewplay.crewplay_backend.domain.match.*;
import in.crewplay.crewplay_backend.domain.match.innings.MatchInnings;
import in.crewplay.crewplay_backend.domain.match.innings.dto.*;
import in.crewplay.crewplay_backend.domain.match.innings.repository.MatchInningsRepository;
import in.crewplay.crewplay_backend.domain.match.repository.MatchRepository;
import in.crewplay.crewplay_backend.domain.match.repository.MatchSquadMemberRepository;
import in.crewplay.crewplay_backend.domain.match.repository.MatchTeamRepository;
import in.crewplay.crewplay_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InningsService {

    private final MatchRepository matchRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final MatchSquadMemberRepository squadRepository;
    private final MatchInningsRepository matchInningsRepository;
    private final UserRepository userRepository;

    /**
     * STEP 1 — Provide players for striker/bowler selection
     */
    public InningsContextResponse getInningsContext(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (match.getStatus() != MatchStatus.TOSS_DONE) {
            throw new IllegalStateException("Innings not available yet");
        }

        MatchTeam battingTeam = matchTeamRepository
                .findByMatch_IdAndTeam_Id(matchId, match.getBattingTeamId())
                .orElseThrow(() -> new RuntimeException("Batting team not found"));

        MatchTeam bowlingTeam = matchTeamRepository
                .findByMatch_IdAndTeam_Id(matchId, match.getBowlingTeamId())
                .orElseThrow(() -> new RuntimeException("Bowling team not found"));

        InningsContextResponse response = new InningsContextResponse();
        response.setBattingTeam(buildTeamDTO(battingTeam));
        response.setBowlingTeam(buildTeamDTO(bowlingTeam));

        return response;
    }

    private InningsTeamDTO buildTeamDTO(MatchTeam matchTeam) {

        InningsTeamDTO dto = new InningsTeamDTO();
        dto.setTeamId(matchTeam.getTeam().getId());
        dto.setTeamName(matchTeam.getTeam().getName());
        dto.setLogoUrl(matchTeam.getTeam().getLogoUrl());

        List<InningsPlayerDTO> players = squadRepository
                .findByMatchTeam(matchTeam)
                .stream()
                .map(member -> {
                    InningsPlayerDTO p = new InningsPlayerDTO();
                    p.setUserId(member.getUser().getId());
                    p.setName(member.getUser().getEmail()); // Replace later with real name
                    return p;
                })
                .toList();

        dto.setPlayers(players);
        return dto;
    }

    /**
     * STEP 2 — Start innings
     */
    @Transactional
    public void startInnings(
            Long matchId,
            Long scorerUserId,
            StartInningsRequest request
    ) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!match.getScorerUserId().equals(scorerUserId)) {
            throw new IllegalStateException("Only scorer can start innings");
        }

        if (match.getStatus() != MatchStatus.TOSS_DONE) {
            throw new IllegalStateException("Match not ready for innings");
        }

        // 1. Validation for unique selections
        if (request.getStrikerUserId().equals(request.getNonStrikerUserId())) {
            throw new IllegalStateException("Striker and non-striker cannot be the same");
        }

        if (request.getBowlerUserId().equals(request.getStrikerUserId()) ||
                request.getBowlerUserId().equals(request.getNonStrikerUserId())) {
            throw new IllegalStateException("Bowler cannot be one of the opening batsmen");
        }

// 2. Fetch MatchTeams to validate squads
        MatchTeam battingTeam = matchTeamRepository
                .findByMatch_IdAndTeam_Id(matchId, match.getBattingTeamId())
                .orElseThrow(() -> new RuntimeException("Batting team not found for this match"));

        MatchTeam bowlingTeam = matchTeamRepository
                .findByMatch_IdAndTeam_Id(matchId, match.getBowlingTeamId())
                .orElseThrow(() -> new RuntimeException("Bowling team not found for this match"));

// 3. Squad Validation using correct DTO getters
        boolean strikerValid = squadRepository
                .existsByMatchTeamAndUser_Id(battingTeam, request.getStrikerUserId());

        boolean nonStrikerValid = squadRepository
                .existsByMatchTeamAndUser_Id(battingTeam, request.getNonStrikerUserId());

        boolean bowlerValid = squadRepository
                .existsByMatchTeamAndUser_Id(bowlingTeam, request.getBowlerUserId());

        if (!strikerValid || !nonStrikerValid) {
            throw new IllegalStateException("Selected batsmen must belong to the batting team squad");
        }

        if (!bowlerValid) {
            throw new IllegalStateException("Selected bowler must belong to the bowling team squad");
        }

// 4. Initialize the Innings
        MatchInnings innings = new MatchInnings();
        innings.setMatch(match);
        innings.setInningsNumber(1);

// Map User entities via References (performance efficient)
        innings.setStriker(userRepository.getReferenceById(request.getStrikerUserId()));
        innings.setNonStriker(userRepository.getReferenceById(request.getNonStrikerUserId()));
        innings.setCurrentBowler(userRepository.getReferenceById(request.getBowlerUserId()));

// Map Hands and Types
        innings.setStrikerHand(request.getStrikerHand());
        innings.setNonStrikerHand(request.getNonStrikerHand());
        innings.setBowlerType(request.getBowlingType()); // Matches DTO field 'bowlingType'

        matchInningsRepository.save(innings);

        match.setStatus(MatchStatus.LIVE);
        matchRepository.save(match);
    }
}