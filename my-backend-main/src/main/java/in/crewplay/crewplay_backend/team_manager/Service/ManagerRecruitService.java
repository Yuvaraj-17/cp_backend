package in.crewplay.crewplay_backend.team_manager.Service;

import in.crewplay.crewplay_backend.common.exception.ApiException;
import in.crewplay.crewplay_backend.domain.match.scoring.entity.BallEvent;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.BallResultType;
import in.crewplay.crewplay_backend.domain.match.scoring.repository.BallEventRepository;
import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.domain.teams.TeamJoinRequest;
import in.crewplay.crewplay_backend.domain.teams.enums.JoinRequestStatus;
import in.crewplay.crewplay_backend.domain.teams.enums.JoinRequestType;
import in.crewplay.crewplay_backend.domain.user.PlayerProfile;
import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.domain.user.repository.PlayerProfileRepository;
import in.crewplay.crewplay_backend.domain.user.repository.UserRepository;
import in.crewplay.crewplay_backend.team.repository.TeamJoinRequestRepository;
import in.crewplay.crewplay_backend.team.repository.TeamRepository;
import in.crewplay.crewplay_backend.team_roster.repository.TeamMemberRepository;
import in.crewplay.crewplay_backend.team_manager.dto.response.DiscoverPlayerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerRecruitService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamJoinRequestRepository joinRequestRepository;
    private final BallEventRepository ballEventRepository;
    private final PlayerProfileRepository playerProfileRepository;

    // ─────────────────────────────────────────────
    // DISCOVER PLAYERS
    // ─────────────────────────────────────────────

    public List<DiscoverPlayerResponse> discoverPlayers(Long managerUserId,
                                                        Long teamId,
                                                        String query,
                                                        String role) {

        Team team = getTeamAndVerifyManager(managerUserId, teamId);

        List<Long> memberUserIds = teamMemberRepository.findByTeam(team).stream()
                .filter(m -> m.getUser() != null)
                .map(m -> m.getUser().getId())
                .collect(Collectors.toList());

        List<User> candidates;

        if (query != null && !query.isBlank()) {
            candidates = userRepository.searchByEmail(query, managerUserId, memberUserIds);
        } else {
            candidates = userRepository.findAllExcluding(managerUserId, memberUserIds);
        }

        if (candidates.size() > 50) {
            candidates = candidates.subList(0, 50);
        }

        List<Long> ids = candidates.stream().map(User::getId).toList();
        Map<Long, PlayerProfile> profileMap = playerProfileRepository
                .findByIdIn(ids)
                .stream()
                .collect(Collectors.toMap(PlayerProfile::getId, p -> p));

        return candidates.stream()
                .map(u -> buildCard(u, team, profileMap.get(u.getId())))
                .filter(card -> matchesRoleFilter(card, role))
                .sorted(Comparator.comparingInt(DiscoverPlayerResponse::getOverallRating).reversed())
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // SEND INVITE
    // ─────────────────────────────────────────────

    @Transactional
    public void sendInvite(Long managerUserId, Long teamId, Long playerId) {

        Team team = getTeamAndVerifyManager(managerUserId, teamId);

        User player = userRepository.findById(playerId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "Player not found"));

        if (teamMemberRepository.existsByTeamAndUser(team, player)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Player is already a member of this team");
        }

        Optional<TeamJoinRequest> existingInvite =
                joinRequestRepository.findByTeamAndPlayerAndStatus(
                        team, player, JoinRequestStatus.PENDING);

        if (existingInvite.isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Invite already pending. Expires on "
                            + existingInvite.get().getExpiresAt());
        }

        User manager = userRepository.getReferenceById(managerUserId);

        TeamJoinRequest invite = new TeamJoinRequest();
        invite.setTeam(team);
        invite.setPlayer(player);
        invite.setInitiatedBy(manager);
        invite.setType(JoinRequestType.MANAGER_INVITE);
        invite.setStatus(JoinRequestStatus.PENDING);
        invite.setCreatedAt(LocalDateTime.now());
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));

        joinRequestRepository.save(invite);
    }

    // ─────────────────────────────────────────────
    // BUILD CARD
    // ─────────────────────────────────────────────

    private DiscoverPlayerResponse buildCard(User user,
                                             Team team,
                                             PlayerProfile profile) {

        List<BallEvent> battingEvents = ballEventRepository.findByBatsman(user)
                .stream()
                .filter(e -> !e.isUndone())
                .toList();

        int totalRuns = battingEvents.stream()
                .mapToInt(BallEvent::getRunsOffBat)
                .sum();

        long dismissals = battingEvents.stream()
                .filter(e -> e.isWicket()
                        && e.getDismissedPlayer() != null
                        && e.getDismissedPlayer().getId().equals(user.getId()))
                .count();

        double average = dismissals > 0
                ? (double) totalRuns / dismissals
                : (double) totalRuns;

        int ballsFaced = battingEvents.size();

        double strikeRate = ballsFaced > 0
                ? (double) totalRuns / ballsFaced * 100.0
                : 0.0;

        List<BallEvent> bowlingEvents = ballEventRepository.findByBowler(user)
                .stream()
                .filter(e -> !e.isUndone())
                .toList();

        int wickets = (int) bowlingEvents.stream()
                .filter(BallEvent::isWicket)
                .count();

        List<BallEvent> legalBalls = bowlingEvents.stream()
                .filter(e -> e.getBallResultType() == BallResultType.NORMAL)
                .toList();

        int runsConceded = legalBalls.stream()
                .mapToInt(e -> e.getRunsOffBat() + e.getExtraRuns())
                .sum();

        double economy = !legalBalls.isEmpty()
                ? runsConceded / (legalBalls.size() / 6.0)
                : 0.0;

        Set<Long> matchIds = new HashSet<>();
        battingEvents.forEach(e -> matchIds.add(e.getMatch().getId()));
        bowlingEvents.forEach(e -> matchIds.add(e.getMatch().getId()));

        int totalMatches = matchIds.size();

        String playingRole = deriveRole(profile,
                battingEvents.size(),
                bowlingEvents.size());

        int ovr = computeOvr(totalRuns, average, strikeRate, wickets, economy);

        String inviteStatus = joinRequestRepository
                .findByTeamAndPlayerAndStatus(team, user, JoinRequestStatus.PENDING)
                .map(r -> "PENDING")
                .orElse(null);

        String displayName = profile != null && profile.getName() != null
                ? profile.getName()
                : nameFromEmail(user);

        return DiscoverPlayerResponse.builder()
                .userId(user.getId())
                .name(displayName)
                .playingRole(playingRole)
                .overallRating(ovr)
                .totalMatches(totalMatches)
                .battingAverage(round(average))
                .strikeRate(round(strikeRate))
                .totalWickets(wickets)
                .economy(round(economy))
                .inviteStatus(inviteStatus)
                .build();
    }

    // ─────────────────────────────────────────────
    // OVR FORMULA
    // ─────────────────────────────────────────────

    private int computeOvr(int runs, double avg,
                           double sr, int wickets, double eco) {

        double batting = Math.min(20, runs / 20.0)
                + Math.min(20, avg * 0.4)
                + Math.min(20, sr / 8.0);

        double bowling = Math.min(30, wickets * 2.5)
                + (eco > 0 ? Math.max(0, 10 - eco) : 0);

        return (int) Math.max(50, Math.min(99, batting + bowling));
    }

    private String deriveRole(PlayerProfile profile,
                              int batCount,
                              int bowlCount) {

        if (profile != null && profile.getPlayingRole() != null) {
            return profile.getPlayingRole().name();
        }

        int total = batCount + bowlCount;
        if (total == 0) return "PLAYER";

        double batPct = (double) batCount / total;
        if (batPct > 0.70) return "BATSMAN";
        if (batPct < 0.30) return "BOWLER";
        return "ALL_ROUNDER";
    }

    private boolean matchesRoleFilter(DiscoverPlayerResponse card,
                                      String filter) {

        if (filter == null || filter.isBlank()
                || "ALL".equalsIgnoreCase(filter))
            return true;

        return filter.equalsIgnoreCase(card.getPlayingRole());
    }

    private String nameFromEmail(User user) {
        if (user.getEmail() != null && user.getEmail().contains("@"))
            return user.getEmail().split("@")[0];
        if (user.getMobileNumber() != null)
            return user.getMobileNumber();
        return "Player #" + user.getId();
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private Team getTeamAndVerifyManager(Long managerUserId,
                                         Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "Team not found"));

        if (!managerUserId.equals(team.getManagerUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "You do not manage this team");
        }

        return team;
    }
}
