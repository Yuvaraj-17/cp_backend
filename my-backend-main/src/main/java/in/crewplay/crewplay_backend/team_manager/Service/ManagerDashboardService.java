package in.crewplay.crewplay_backend.team_manager.Service;

import in.crewplay.crewplay_backend.common.exception.ApiException;
import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.repository.MatchRepository;
import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.domain.user.TeamManagerProfile;
import in.crewplay.crewplay_backend.team.enums.JoinRequestStatusForOverAllTeam;
import in.crewplay.crewplay_backend.team.repository.PlayerAvailabilityRepository;
import in.crewplay.crewplay_backend.team.repository.TeamJoinRequestRepository;
import in.crewplay.crewplay_backend.team_manager.Repository.TeamManagerProfileRepository;
import in.crewplay.crewplay_backend.team_manager.dto.response.LeagueUpdateResponse;
import in.crewplay.crewplay_backend.team_manager.dto.response.UpcomingMatchCard;
import in.crewplay.crewplay_backend.team_manager.dto.response.ManagerDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerDashboardService {

    private final TeamManagerProfileRepository profileRepository;
    private final MatchRepository matchRepository;
    private final TeamJoinRequestRepository joinRequestRepository;
    private final PlayerAvailabilityRepository playerAvailabilityRepository;

    // ─────────────────────────────────────────────────────────────
    // PUBLIC ENTRY POINT
    // ─────────────────────────────────────────────────────────────

    public ManagerDashboardResponse buildDashboard(Long managerUserId) {

        TeamManagerProfile profile = profileRepository
                .findByUser_Id(managerUserId) // ⚠️ MUST exist in repository
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND,
                                "Team manager profile not found"));

        Team activeTeam = profile.getActiveTeam();

        // No active team → onboarding state
        if (activeTeam == null) {
            return ManagerDashboardResponse.builder()
                    .upcomingMatch(null)
                    .pendingApprovalsCount(0)
                    .leagueUpdates(staticLeagueUpdates())
                    .build();
        }

        // ── Upcoming Match ─────────────────────────────

        LocalDateTime now = LocalDateTime.now();

        UpcomingMatchCard upcomingCard = matchRepository
                .findNextUpcomingForTeam(activeTeam.getId(), now)
                .map(match -> buildMatchCard(match, activeTeam))
                .orElse(null);

        // ── Pending Approvals ──────────────────────────

        int pendingCount = joinRequestRepository
                .countByTeamAndStatus(
                        activeTeam,
                        JoinRequestStatusForOverAllTeam.PENDING
                );

        return ManagerDashboardResponse.builder()
                .upcomingMatch(upcomingCard)
                .pendingApprovalsCount(pendingCount)
                .leagueUpdates(staticLeagueUpdates())
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    private UpcomingMatchCard buildMatchCard(Match match, Team activeTeam) {

        boolean isTeamA =
                match.getTeamA() != null &&
                        match.getTeamA().getId().equals(activeTeam.getId());

        String opponentName = isTeamA
                ? (match.getTeamB() != null
                ? match.getTeamB().getName()
                : "TBD")
                : (match.getTeamA() != null
                ? match.getTeamA().getName()
                : "TBD");

        String formattedDateTime =
                match.getScheduledAt() != null
                        ? match.getScheduledAt()
                        .format(DateTimeFormatter.ofPattern("EEE d MMM, h:mm a"))
                        : "Date TBD";

        boolean squadSubmitted =
                playerAvailabilityRepository
                        .countByMatchAndTeamAndIsInPlayingXiTrue(match, activeTeam) > 0;

        return UpcomingMatchCard.builder()
                .matchId(match.getId())
                .homeTeamName(activeTeam.getName())
                .opponentTeamName(opponentName)
                .matchDateTime(formattedDateTime)
                .venue(match.getCity())
                .matchType(match.getMatchType() != null
                        ? match.getMatchType().name()
                        : "FRIENDLY")
                .matchStatus(match.getStatus().name())
                .squadSubmitted(squadSubmitted)
                .submissionDeadline(deadlineLabel(match.getScheduledAt()))
                .build();
    }

    private String deadlineLabel(LocalDateTime scheduledAt) {

        if (scheduledAt == null) return "No date set";

        LocalDateTime deadline = scheduledAt.minusHours(2);
        Duration remaining = Duration.between(LocalDateTime.now(), deadline);

        if (remaining.isNegative()) return "Deadline passed";
        if (remaining.toHours() > 0) return remaining.toHours() + "h remaining";
        if (remaining.toMinutes() > 0) return remaining.toMinutes() + "m remaining";

        return "Submitting now";
    }

    private List<LeagueUpdateResponse> staticLeagueUpdates() {

        return List.of(
                LeagueUpdateResponse.builder()
                        .leagueName("Premier League 2025")
                        .status("OPEN")
                        .subtitle("Registrations close soon — secure your spot.")
                        .build(),
                LeagueUpdateResponse.builder()
                        .leagueName("Friendly Series")
                        .status("ACTIVE")
                        .subtitle("Find local opponents for practice matches.")
                        .build()
        );
    }
}
