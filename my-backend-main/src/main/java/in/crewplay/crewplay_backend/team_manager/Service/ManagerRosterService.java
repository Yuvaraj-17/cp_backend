package in.crewplay.crewplay_backend.team_manager.Service;

import in.crewplay.crewplay_backend.domain.teams.*;
import in.crewplay.crewplay_backend.domain.teams.enums.JoinRequestStatus;
import in.crewplay.crewplay_backend.domain.teams.enums.JoinRequestType;
import in.crewplay.crewplay_backend.common.enums.PlayerAddMethod;
import in.crewplay.crewplay_backend.team.repository.TeamJoinRequestRepository;
import in.crewplay.crewplay_backend.team_roster.repository.TeamMemberRepository;
import in.crewplay.crewplay_backend.team.repository.TeamRepository;
import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.domain.user.repository.UserRepository;
import in.crewplay.crewplay_backend.team_manager.dto.request.AddGuestRequest;
import in.crewplay.crewplay_backend.team_manager.dto.request.InvitePlayerRequest;
import in.crewplay.crewplay_backend.team_manager.dto.response.RosterPlayerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles roster management for Team Manager.
 * Includes invites, guest additions, roster view, and removal.
 */
@Service
@RequiredArgsConstructor
public class ManagerRosterService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamJoinRequestRepository joinRequestRepository;

    /**
     * Invite existing platform user by mobile number.
     */
    @Transactional
    public String invitePlayerByMobile(Long managerUserId,
                                       Long teamId,
                                       InvitePlayerRequest req) {

        if (req.getMobileNumber() == null || req.getMobileNumber().isBlank())
            throw new IllegalArgumentException("Mobile number required");

        Team team = getTeamAndVerify(managerUserId, teamId);

        User manager = userRepository.findById(managerUserId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        Optional<User> playerOpt = userRepository.findByMobileNumber(req.getMobileNumber());
        if (playerOpt.isEmpty())
            return "PLAYER_NOT_FOUND";

        User player = playerOpt.get();

        if (teamMemberRepository.existsByTeamAndUser(team, player))
            throw new IllegalStateException("Player already in team");

        // Check existing pending invite
        joinRequestRepository
                .findByTeamAndPlayerAndStatus(team, player, JoinRequestStatus.PENDING)
                .ifPresent(r -> {
                    if (r.getExpiresAt() != null && r.getExpiresAt().isAfter(LocalDateTime.now()))
                        throw new IllegalStateException("Invite already pending");
                });

        TeamJoinRequest invite = new TeamJoinRequest();
        invite.setTeam(team);
        invite.setPlayer(player);
        invite.setInitiatedBy(manager);
        invite.setType(JoinRequestType.MANAGER_INVITE);
        invite.setStatus(JoinRequestStatus.PENDING);
        invite.setMessage(req.getMessage());
        invite.setCreatedAt(LocalDateTime.now());
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));

        joinRequestRepository.save(invite);

        return "INVITED";
    }

    /**
     * Add manual guest player (no platform account).
     */
    @Transactional
    public TeamMember addGuestPlayer(Long managerUserId,
                                     Long teamId,
                                     AddGuestRequest req) {

        if (req.getDisplayName() == null || req.getDisplayName().isBlank())
            throw new IllegalArgumentException("Display name required");

        Team team = getTeamAndVerify(managerUserId, teamId);

        // Prevent duplicate guest by mobile
        if (req.getMobileNumber() != null &&
                teamMemberRepository.existsByTeam_IdAndMobileNumber(teamId, req.getMobileNumber()))
            throw new IllegalStateException("Guest with this mobile already exists");

        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(null);
        member.setDisplayName(req.getDisplayName());
        member.setMobileNumber(req.getMobileNumber());
        member.setIsGuest(true);
        member.setAddMethod(PlayerAddMethod.MANUAL_GUEST);
        member.setAddedByScorerId(managerUserId);
        member.setCreatedAt(LocalDateTime.now());

        return teamMemberRepository.save(member);
    }

    /**
     * Fetch full roster.
     */
    public List<RosterPlayerResponse> getRoster(Long managerUserId,
                                                Long teamId) {

        Team team = getTeamAndVerify(managerUserId, teamId);

        return teamMemberRepository.findByTeam_Id(teamId)
                .stream()
                .map(member -> RosterPlayerResponse.builder()
                        .memberId(member.getId())
                        .userId(member.getUser() != null ? member.getUser().getId() : null)
                        .displayName(member.getDisplayName())
                        .mobileNumber(member.getMobileNumber())
                        .addMethod(member.getAddMethod().name())
                        .isGuest(Boolean.TRUE.equals(member.getIsGuest()))
                        .verificationStatus(resolveVerificationStatus(member))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Remove player from team.
     */
    @Transactional
    public void removePlayer(Long managerUserId,
                             Long teamId,
                             Long memberId) {

        getTeamAndVerify(managerUserId, teamId);

        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!member.getTeam().getId().equals(teamId))
            throw new IllegalStateException("Member does not belong to this team");

        teamMemberRepository.delete(member);
    }

    /**
     * Ensures manager owns the team.
     */
    private Team getTeamAndVerify(Long managerUserId, Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!managerUserId.equals(team.getManagerUserId()))
            throw new IllegalStateException("Access denied");

        return team;
    }

    /**
     * Determines verification label for UI.
     */
    private String resolveVerificationStatus(TeamMember member) {

        if (Boolean.TRUE.equals(member.getIsGuest()))
            return "Guest";

        if (member.getUser() != null)
            return "Verified";

        return "Pending";
    }
}
