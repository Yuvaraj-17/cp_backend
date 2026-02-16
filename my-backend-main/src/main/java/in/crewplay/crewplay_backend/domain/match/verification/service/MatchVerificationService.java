package in.crewplay.crewplay_backend.domain.match.verification.service;

import in.crewplay.crewplay_backend.domain.match.*;
import in.crewplay.crewplay_backend.domain.match.repository.MatchRepository;
import in.crewplay.crewplay_backend.domain.match.repository.MatchTeamRepository;
import in.crewplay.crewplay_backend.domain.match.repository.MatchSquadMemberRepository;
import in.crewplay.crewplay_backend.domain.match.verification.MatchVerification;
import in.crewplay.crewplay_backend.domain.match.verification.MatchVerificationStatus;
import in.crewplay.crewplay_backend.domain.match.verification.dto.*;
import in.crewplay.crewplay_backend.domain.match.verification.repository.MatchVerificationRepository;
import in.crewplay.crewplay_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchVerificationService {

    private final MatchRepository matchRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final MatchSquadMemberRepository squadMemberRepository;
    private final MatchVerificationRepository matchVerificationRepository;
    private final UserRepository userRepository;

    public VerificationContextResponse getVerificationContext(Long matchId, Long scorerUserId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (match.getStatus() != MatchStatus.AWAITING_VERIFICATION) {
            throw new IllegalStateException("Match not ready for verification");
        }

        List<MatchTeam> matchTeams = matchTeamRepository.findByMatch(match);

        MatchTeam teamA = matchTeams.stream()
                .filter(mt -> "A".equals(mt.getSide()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Team A not found"));

        MatchTeam teamB = matchTeams.stream()
                .filter(mt -> "B".equals(mt.getSide()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Team B not found"));

        VerificationContextResponse response = new VerificationContextResponse();
        response.setTeamA(buildTeamDTO(teamA, scorerUserId));
        response.setTeamB(buildTeamDTO(teamB, scorerUserId));

        return response;
    }

    private VerificationTeamDTO buildTeamDTO(MatchTeam matchTeam, Long scorerUserId) {

        VerificationTeamDTO dto = new VerificationTeamDTO();
        dto.setMatchTeamId(matchTeam.getId());
        dto.setTeamName(matchTeam.getTeam().getName());
        dto.setLogoUrl(matchTeam.getTeam().getLogoUrl());

        List<VerificationPlayerDTO> players = squadMemberRepository
                .findByMatchTeam(matchTeam)
                .stream()
                .map(member -> {

                    VerificationPlayerDTO playerDTO = new VerificationPlayerDTO();
                    playerDTO.setUserId(member.getUser().getId());
                    playerDTO.setName(member.getUser().getEmail()); // or better name if available
                    playerDTO.setGuest(false); // squad only contains registered users
                    playerDTO.setScorer(member.getUser().getId().equals(scorerUserId));

                    return playerDTO;
                })
                .toList();

        dto.setPlayers(players);

        return dto;
    }

    @Transactional
    public void sendVerificationRequest(
            Long scorerUserId,
            Long matchId,
            Long matchTeamId,
            Long selectedUserId
    ) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (match.getStatus() != MatchStatus.AWAITING_VERIFICATION) {
            throw new IllegalStateException("Match not in verification stage");
        }

        if (!match.getScorerUserId().equals(scorerUserId)) {
            throw new IllegalStateException("Only scorer can send verification");
        }

        if (scorerUserId.equals(selectedUserId)) {
            throw new IllegalStateException("Scorer cannot verify his own match");
        }

        MatchTeam matchTeam = matchTeamRepository.findById(matchTeamId)
                .orElseThrow(() -> new RuntimeException("Match team not found"));

        // Ensure player is in squad
        boolean inSquad = squadMemberRepository.findByMatchTeam(matchTeam)
                .stream()
                .anyMatch(member -> member.getUser().getId().equals(selectedUserId));

        if (!inSquad) {
            throw new IllegalStateException("Selected player not in squad");
        }

        // Prevent duplicate request per team
        if (matchVerificationRepository.findByMatchAndMatchTeam(match, matchTeam).isPresent()) {
            throw new IllegalStateException("Verification already sent for this team");
        }

        MatchVerification verification = new MatchVerification();
        verification.setMatch(match);
        verification.setMatchTeam(matchTeam);
        verification.setVerifier(
                userRepository.getReferenceById(selectedUserId)
        );
        verification.setStatus(MatchVerificationStatus.PENDING);

        matchVerificationRepository.save(verification);
    }

    @Transactional
    public void acceptVerification(Long matchId, Long playerUserId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        MatchVerification verification = matchVerificationRepository
                .findByMatch(match)
                .stream()
                .filter(v -> v.getVerifier().getId().equals(playerUserId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Verification request not found"));

        verification.setStatus(MatchVerificationStatus.VERIFIED);
        verification.setRespondedAt(java.time.LocalDateTime.now());

        matchVerificationRepository.save(verification);

        checkAndPromoteMatch(match);
    }

    @Transactional
    public void rejectVerification(Long matchId, Long playerUserId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        MatchVerification verification = matchVerificationRepository
                .findByMatch(match)
                .stream()
                .filter(v -> v.getVerifier().getId().equals(playerUserId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Verification request not found"));

        verification.setStatus(MatchVerificationStatus.REJECTED);
        verification.setRespondedAt(java.time.LocalDateTime.now());

        matchVerificationRepository.save(verification);

        match.setStatus(MatchStatus.DRAFT); // rollback stage
        matchRepository.save(match);
    }

    private void checkAndPromoteMatch(Match match) {

        boolean hasPending = matchVerificationRepository
                .existsByMatchAndStatus(match, MatchVerificationStatus.PENDING);

        if (!hasPending) {
            match.setStatus(MatchStatus.VERIFIED);
            matchRepository.save(match);
        }
    }



}
