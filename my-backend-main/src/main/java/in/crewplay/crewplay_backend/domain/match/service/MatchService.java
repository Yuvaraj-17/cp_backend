package in.crewplay.crewplay_backend.domain.match.service;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.MatchStatus;
import in.crewplay.crewplay_backend.domain.match.MatchSquadMember;
import in.crewplay.crewplay_backend.domain.match.MatchTeam;
import in.crewplay.crewplay_backend.domain.match.dto.SquadPlayerDTO;
import in.crewplay.crewplay_backend.domain.match.dto.*;
import in.crewplay.crewplay_backend.domain.match.repository.MatchRepository;
import in.crewplay.crewplay_backend.domain.match.repository.MatchSquadMemberRepository;
import in.crewplay.crewplay_backend.domain.match.repository.MatchTeamRepository;
import in.crewplay.crewplay_backend.domain.user.repository.UserRepository;
import in.crewplay.crewplay_backend.team.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.in;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final MatchSquadMemberRepository squadMemberRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    /**
     * 🔥 SINGLE ENTRY POINT — PHASE-0
     * Called when scorer clicks "Start Match"
     */
    @Transactional
    public Match startMatch(Long scorerId, SubmitSquadRequest request) {

        // 1️⃣ VALIDATION (Phase-0 invariants)
        validateSquads(
                request.getTeamA().stream().map(SquadPlayerDTO::getUserId).toList(),
                request.getTeamB().stream().map(SquadPlayerDTO::getUserId).toList(),
                request.getTeamA().stream().filter(SquadPlayerDTO::isCaptain).count(),
                request.getTeamB().stream().filter(SquadPlayerDTO::isCaptain).count()
        );

        // 2️⃣ CREATE MATCH
        Match match = new Match();
        match.setScorerUserId(scorerId);
        match.setCity(request.getCity()); // optional in Phase-0
        match.setStatus(MatchStatus.READY); // NOT LIVE
        match = matchRepository.save(match);

        // 3️⃣ TEAM A + SQUAD
        saveTeamAndSquad(
                match,
                request.getTeamAId(),
                request.getTeamA(),
                "A"
        );

        // 4️⃣ TEAM B + SQUAD
        saveTeamAndSquad(
                match,
                request.getTeamBId(),
                request.getTeamB(),
                "B"
        );

        return match;
    }

    /**
     * 🔒 Phase-0 squad rules
     */
    private void validateSquads(
            List<Long> teamAPlayers,
            List<Long> teamBPlayers,
            long teamACaptains,
            long teamBCaptains
    ) {
        if (teamAPlayers.size() < 3 || teamBPlayers.size() < 3) {
            throw new IllegalStateException("Each team must have at least 3 players");
        }

        Set<Long> intersection = new HashSet<>(teamAPlayers);
        intersection.retainAll(teamBPlayers);

        if (!intersection.isEmpty()) {
            throw new IllegalStateException("Same player cannot play for both teams");
        }

        if (teamACaptains != 1 || teamBCaptains != 1) {
            throw new IllegalStateException("Each team must have exactly one captain");
        }
    }

    /**
     * 🧩 Helper — persists MatchTeam + MatchSquadMembers
     */
    private void saveTeamAndSquad(
            Match match,
            Long teamId,
            List<SquadPlayerDTO> players,
            String side
    ) {
        // Create MatchTeam
        MatchTeam matchTeam = new MatchTeam();
        matchTeam.setMatch(match);
        matchTeam.setTeam(teamRepository.getReferenceById(teamId));
        matchTeam.setSide(side);
        matchTeam = matchTeamRepository.save(matchTeam);

        // Save squad members
        for (SquadPlayerDTO dto : players) {
            MatchSquadMember member = new MatchSquadMember();
            member.setMatchTeam(matchTeam);
            member.setUser(userRepository.getReferenceById(dto.getUserId()));
            member.setCaptain(dto.isCaptain());
            member.setWicketKeeper(dto.isWicketKeeper());
            squadMemberRepository.save(member);
        }
    }
}
