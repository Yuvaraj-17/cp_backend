package in.crewplay.crewplay_backend.team_manager.Service;

import in.crewplay.crewplay_backend.domain.teams.*;
import in.crewplay.crewplay_backend.common.enums.PlayerAddMethod;
import in.crewplay.crewplay_backend.domain.teams.enums.JoinRequestStatus;
import in.crewplay.crewplay_backend.team.enums.JoinRequestStatusForOverAllTeam;
import in.crewplay.crewplay_backend.team.repository.TeamJoinRequestRepository;
import in.crewplay.crewplay_backend.team.repository.TeamRepository;
import in.crewplay.crewplay_backend.team_roster.repository.TeamMemberRepository;
import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.team_manager.dto.response.ApprovalsOverviewResponse;
import in.crewplay.crewplay_backend.team_manager.dto.response.PendingApprovalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles approval and rejection of team join requests.
 */
@Service
@RequiredArgsConstructor
public class ManagerApprovalService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final TeamRepository teamRepository;
    private final TeamJoinRequestRepository joinRequestRepository;
    private final TeamMemberRepository teamMemberRepository;

    /**
     * Returns pending requests with header statistics.
     */
    public ApprovalsOverviewResponse getApprovals(Long managerUserId, Long teamId) {

        Team team = getTeamAndVerify(managerUserId, teamId);
        LocalDateTime now = LocalDateTime.now();

        List<PendingApprovalResponse> requests =
                joinRequestRepository
                        .findByTeamAndStatus(team, JoinRequestStatusForOverAllTeam.PENDING)
                        .stream()
                        .map(r -> buildCard(r, now))
                        .collect(Collectors.toList());

        return ApprovalsOverviewResponse.builder()
                .totalPending(
                        joinRequestRepository.countByTeamAndStatus(team, JoinRequestStatusForOverAllTeam.PENDING)
                )
                .newToday(
                        joinRequestRepository.countNewSince(
                                team,
                                JoinRequestStatusForOverAllTeam.PENDING,
                                now.toLocalDate().atStartOfDay()
                        )
                )
                .expiringSoon(
                        joinRequestRepository.countExpiringBetween(
                                team,
                                JoinRequestStatusForOverAllTeam.PENDING,
                                now,
                                now.plusHours(24)
                        )
                )
                .requests(requests)
                .build();
    }

    /**
     * Approves a join request and adds player to roster.
     */
    @Transactional
    public void approveRequest(Long managerUserId,
                               Long teamId,
                               Long requestId) {

        Team team = getTeamAndVerify(managerUserId, teamId);
        TeamJoinRequest req = getRequestAndVerify(requestId, team);

        if (req.getExpiresAt() != null &&
                req.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new IllegalStateException("request has expired");

        User player = req.getPlayer();

        if (teamMemberRepository.existsByTeamAndUser(team, player)) {

            TeamMember member = new TeamMember();
            member.setTeam(team);
            member.setUser(player);
            member.setMobileNumber(
                    player.getMobileNumber() != null ? player.getMobileNumber() : ""
            );

            String displayName =
                    (player.getEmail() != null && player.getEmail().contains("@"))
                            ? player.getEmail().split("@")[0]
                            : "Player";

            member.setDisplayName(displayName);
            member.setIsGuest(false);
            member.setAddMethod(PlayerAddMethod.TEAM_CODE);
            member.setAddedByScorerId(managerUserId);
            member.setCreatedAt(LocalDateTime.now());

            teamMemberRepository.save(member);
        }

        req.setStatus(JoinRequestStatus.APPROVED);
        req.setRespondedAt(LocalDateTime.now());
        joinRequestRepository.save(req);
    }

    /**
     * Rejects a join request.
     */
    @Transactional
    public void rejectRequest(Long managerUserId,
                              Long teamId,
                              Long requestId) {

        Team team = getTeamAndVerify(managerUserId, teamId);
        TeamJoinRequest req = getRequestAndVerify(requestId, team);

        req.setStatus(JoinRequestStatus.REJECTED);
        req.setRespondedAt(LocalDateTime.now());
        joinRequestRepository.save(req);
    }

    /**
     * Returns approval history (non-pending).
     */
    public List<PendingApprovalResponse> getHistory(Long managerUserId,
                                                    Long teamId) {

        Team team = getTeamAndVerify(managerUserId, teamId);

        return joinRequestRepository
                .findByTeamAndStatusNot(team, JoinRequestStatusForOverAllTeam.PENDING)
                .stream()
                .map(r -> buildCard(r, LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    private PendingApprovalResponse buildCard(TeamJoinRequest r,
                                              LocalDateTime now) {

        User player = r.getPlayer();

        boolean expiringSoon =
                r.getExpiresAt() != null &&
                        r.getExpiresAt().isAfter(now) &&
                        r.getExpiresAt().isBefore(now.plusHours(24));

        String playerName =
                (player.getEmail() != null && player.getEmail().contains("@"))
                        ? player.getEmail().split("@")[0]
                        : "Player";

        return PendingApprovalResponse.builder()
                .requestId(r.getId())
                .playerId(player.getId())
                .playerName(playerName)
                .requestType(r.getType().name())
                .createdAt(r.getCreatedAt().format(DATE_FORMAT))
                .expiresAt(
                        r.getExpiresAt() != null
                                ? r.getExpiresAt().format(DATE_FORMAT)
                                : null
                )
                .isExpiringSoon(expiringSoon)
                .build();
    }

    private Team getTeamAndVerify(Long managerUserId, Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!managerUserId.equals(team.getManagerUserId()))
            throw new IllegalStateException("Access denied");

        return team;
    }

    private TeamJoinRequest getRequestAndVerify(Long requestId,
                                                Team team) {

        TeamJoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("request not found"));

        if (!req.getTeam().getId().equals(team.getId()))
            throw new IllegalStateException("request does not belong to this team");

        if (req.getStatus() != JoinRequestStatus.PENDING)
            throw new IllegalStateException("request is no longer pending");

        return req;
    }
}
