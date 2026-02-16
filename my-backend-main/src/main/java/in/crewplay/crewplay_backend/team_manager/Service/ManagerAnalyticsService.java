package in.crewplay.crewplay_backend.team_manager.Service;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.repository.MatchRepository;
import in.crewplay.crewplay_backend.domain.match.scoring.entity.BallEvent;
import in.crewplay.crewplay_backend.domain.match.scoring.repository.BallEventRepository;
import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.team.repository.TeamRepository;
import in.crewplay.crewplay_backend.team_manager.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerAnalyticsService {

    private final MatchRepository matchRepository;
    private final BallEventRepository ballEventRepository;
    private final TeamRepository teamRepository;

    /**
     * Team analytics for last 5 completed matches.
     */
    public TeamAnalyticsResponse getTeamAnalytics(Long managerUserId, Long teamId) {

        validateManagerOwnership(managerUserId, teamId);

        List<Match> matches =
                matchRepository.findRecentCompletedByTeam(teamId, 5);

        if (matches.isEmpty()) {
            return TeamAnalyticsResponse.builder()
                    .winRate(0.0)
                    .avgScore(0)
                    .netRunRate(0.0)
                    .formTrend(List.of())
                    .topBatters(List.of())
                    .topBowlers(List.of())
                    .build();
        }

        List<Long> matchIds = matches.stream()
                .map(Match::getId)
                .toList();

        List<BallEvent> allEvents =
                ballEventRepository.findByMatch_IdIn(matchIds);

        long wins = matches.stream()
                .filter(m -> didTeamWin(m, teamId))
                .count();

        double winRate =
                (double) wins / matches.size() * 100;

        Map<Long, List<BallEvent>> matchEventMap =
                allEvents.stream()
                        .collect(Collectors.groupingBy(e -> e.getMatch().getId()));

        List<Integer> scores = new ArrayList<>();

        for (Match match : matches) {

            List<BallEvent> events =
                    matchEventMap.getOrDefault(match.getId(), List.of());

            int teamScore = calculateTeamScore(events, teamId);
            scores.add(teamScore);
        }

        int avgScore = (int) scores.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        return TeamAnalyticsResponse.builder()
                .winRate(round(winRate))
                .avgScore(avgScore)
                .formTrend(scores)
                .build();
    }

    /**
     * Match analytics.
     */
    public MatchAnalyticsResponse getMatchAnalytics(Long managerUserId,
                                                    Long teamId,
                                                    Long matchId) {

        validateManagerOwnership(managerUserId, teamId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!match.containsTeam(teamId))
            throw new IllegalStateException("Match does not belong to team");

        if (!match.isCompleted())
            throw new IllegalStateException("Match not completed");

        List<BallEvent> events =
                ballEventRepository.findByMatch(match);

        int teamScore = calculateTeamScore(events, teamId);
        int opponentScore = calculateOpponentScore(events, teamId);

        return MatchAnalyticsResponse.builder()
                .matchId(matchId)
                .teamScore(teamScore)
                .opponentScore(opponentScore)
                .result(teamScore > opponentScore ? "WIN" : "LOSS")
                .build();
    }

    /**
     * Full team stats.
     */
    public FullTeamStatsResponse getFullTeamStats(Long managerUserId,
                                                  Long teamId) {

        validateManagerOwnership(managerUserId, teamId);

        List<Match> matches =
                matchRepository.findCompletedByTeam(teamId);

        List<Long> matchIds =
                matches.stream().map(Match::getId).toList();

        List<BallEvent> events =
                ballEventRepository.findByMatch_IdIn(matchIds);

        Map<Long, Integer> runsByPlayer = new HashMap<>();

        for (BallEvent e : events) {
            if (e.getInnings()
                    .getBattingTeam()
                    .getId()
                    .equals(teamId))
            {

                runsByPlayer.merge(
                        e.getBatsman().getId(),
                        e.getRunsOffBat(),
                        Integer::sum
                );
            }
        }

        List<PlayerStatRow> rows =
                runsByPlayer.entrySet().stream()
                        .map(entry ->
                                PlayerStatRow.builder()
                                        .userId(entry.getKey())
                                        .runs(entry.getValue())
                                        .build())
                        .sorted((a, b) -> b.getRuns() - a.getRuns())
                        .toList();

        return FullTeamStatsResponse.builder()
                .players(rows)
                .build();
    }

    /**
     * Individual player stats.
     */
    public PlayerProfileStatsResponse getPlayerStats(Long managerUserId,
                                                     Long teamId,
                                                     Long playerId) {

        validateManagerOwnership(managerUserId, teamId);

        List<Match> matches =
                matchRepository.findCompletedByTeam(teamId);

        List<Long> matchIds =
                matches.stream().map(Match::getId).toList();

        List<BallEvent> events =
                ballEventRepository.findByMatch_IdIn(matchIds);

        int totalRuns = events.stream()
                .filter(e -> e.getBatsman() != null
                        && e.getBatsman().getId().equals(playerId)
                        && e.getInnings()
                        .getBattingTeam()
                        .getId()
                        .equals(teamId))
                .mapToInt(BallEvent::getRunsOffBat)
                .sum();

        return PlayerProfileStatsResponse.builder()
                .userId(playerId)
                .totalRuns(totalRuns)
                .build();
    }

    // ───── Helper Methods ─────────────────────────────

    private void validateManagerOwnership(Long managerUserId,
                                          Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getManagerUserId().equals(managerUserId))
            throw new IllegalStateException("Access denied");
    }

    private boolean didTeamWin(Match match, Long teamId) {
        return match.getWinnerTeam() != null
                && match.getWinnerTeam().getId().equals(teamId);

    }

    private int calculateTeamScore(List<BallEvent> events,
                                   Long teamId) {

        return events.stream()
                .filter(e -> e.getInnings()
                        .getBattingTeam()
                        .getId()
                        .equals(teamId))
                .mapToInt(e -> e.getRunsOffBat() + e.getExtraRuns())
                .sum();
    }


    private int calculateOpponentScore(List<BallEvent> events,
                                       Long teamId) {

        return events.stream()
                .filter(e -> !e.getInnings()
                        .getBattingTeam()
                        .getId()
                        .equals(teamId))
                .mapToInt(e -> e.getRunsOffBat() + e.getExtraRuns())
                .sum();
    }


    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
