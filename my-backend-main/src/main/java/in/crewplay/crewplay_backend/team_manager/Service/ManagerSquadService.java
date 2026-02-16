package in.crewplay.crewplay_backend.team_manager.Service;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.teams.*;
import in.crewplay.crewplay_backend.domain.teams.enums.AvailabilityStatus;
import in.crewplay.crewplay_backend.team.repository.PlayerAvailabilityRepository;
import in.crewplay.crewplay_backend.team_manager.dto.request.ConfirmPlayingXiRequest;
import in.crewplay.crewplay_backend.team_manager.dto.request.UpdateAvailabilityRequest;
import in.crewplay.crewplay_backend.team_manager.dto.response.SquadPlayerCard;
import in.crewplay.crewplay_backend.team_roster.repository.TeamMemberRepository;
import in.crewplay.crewplay_backend.team.repository.TeamRepository;
import in.crewplay.crewplay_backend.domain.match.repository.MatchRepository;
import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.domain.user.repository.UserRepository;
import in.crewplay.crewplay_backend.team_manager.dto.response.SquadManagementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerSquadService {

    private final MatchRepository matchRepository;
    private final PlayerAvailabilityRepository availabilityRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    /**
     * Returns full squad overview for match.
     */
    public SquadManagementResponse getSquadView(Long managerUserId,
                                                Long teamId,
                                                Long matchId) {

        Team team = getTeamAndVerify(managerUserId, teamId);
        Match match = validateMatchBelongsToTeam(matchId, teamId);

        List<TeamMember> members = teamMemberRepository.findByTeam_Id(teamId);

        Map<Long, PlayerAvailability> availabilityMap =
                availabilityRepository.findByMatchAndTeam(match, team)
                        .stream()
                        .filter(a -> a.getPlayer() != null)
                        .collect(Collectors.toMap(
                                a -> a.getPlayer().getId(),
                                a -> a
                        ));

        int available = 0, unavailable = 0, pending = 0, xiCount = 0;

        List<SquadPlayerCard> cards = new ArrayList<>();
        SquadPlayerCard captainCard = null;
        SquadPlayerCard wkCard = null;

        for (TeamMember member : members) {

            if (member.getUser() == null) continue;

            Long userId = member.getUser().getId();
            PlayerAvailability av = availabilityMap.get(userId);

            AvailabilityStatus status =
                    av != null ? av.getStatus() : AvailabilityStatus.PENDING;

            boolean inXi = av != null && Boolean.TRUE.equals(av.getIsInPlayingXi());
            boolean isCaptain = av != null && Boolean.TRUE.equals(av.getIsCaptain());
            boolean isWk = av != null && Boolean.TRUE.equals(av.getIsWicketKeeper());

            if (status == AvailabilityStatus.AVAILABLE) available++;
            else if (status == AvailabilityStatus.UNAVAILABLE
                    || status == AvailabilityStatus.INJURED) unavailable++;
            else pending++;

            if (inXi) xiCount++;

            SquadPlayerCard card = SquadPlayerCard.builder()
                    .userId(userId)
                    .displayName(member.getDisplayName())
                    .availabilityStatus(status.name())
                    .isInPlayingXi(inXi)
                    .isCaptain(isCaptain)
                    .isWicketKeeper(isWk)
                    .build();

            cards.add(card);

            if (isCaptain) captainCard = card;
            if (isWk) wkCard = card;
        }

        return SquadManagementResponse.builder()
                .matchId(matchId)
                .matchTitle("Match #" + matchId)
                .playingXiSelected(xiCount)
                .playingXiTotal(11)
                .totalSquad(cards.size())
                .available(available)
                .unavailable(unavailable)
                .pending(pending)
                .captain(captainCard)
                .wicketKeeper(wkCard)
                .players(cards)
                .isXiConfirmed(xiCount == 11)
                .build();
    }

    /**
     * Player sets availability for match.
     */
    @Transactional
    public void updatePlayerAvailability(Long playerUserId,
                                         Long teamId,
                                         UpdateAvailabilityRequest req) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Ensure player belongs to team
        if (!teamMemberRepository.existsByTeam_IdAndUser_Id(teamId, playerUserId))
            throw new IllegalStateException("Player not part of this team");

        Match match = validateMatchBelongsToTeam(req.getMatchId(), teamId);

        User player = userRepository.getReferenceById(playerUserId);

        PlayerAvailability availability =
                availabilityRepository
                        .findByMatchAndTeamAndPlayer(match, team, player)
                        .orElseGet(() -> {
                            PlayerAvailability a = new PlayerAvailability();
                            a.setMatch(match);
                            a.setTeam(team);
                            a.setPlayer(player);
                            a.setCreatedAt(LocalDateTime.now());
                            return a;
                        });

        availability.setStatus(req.getStatus());
        availability.setConfirmedAt(LocalDateTime.now());

        availabilityRepository.save(availability);
    }

    /**
     * Confirms Playing XI.
     */
    @Transactional
    public void confirmPlayingXi(Long managerUserId,
                                 Long teamId,
                                 ConfirmPlayingXiRequest req) {

        Team team = getTeamAndVerify(managerUserId, teamId);
        Match match = validateMatchBelongsToTeam(req.getMatchId(), teamId);

        List<Long> selected = req.getPlayerUserIds();

        if (selected == null || selected.size() != 11)
            throw new IllegalArgumentException("Playing XI must contain exactly 11 players");

        if (!selected.contains(req.getCaptainUserId()))
            throw new IllegalArgumentException("Captain must be in Playing XI");

        if (!selected.contains(req.getWicketKeeperUserId()))
            throw new IllegalArgumentException("Wicketkeeper must be in Playing XI");

        Set<Long> teamPlayerIds =
                teamMemberRepository.findByTeam_Id(teamId).stream()
                        .filter(m -> m.getUser() != null)
                        .map(m -> m.getUser().getId())
                        .collect(Collectors.toSet());

        for (Long userId : selected)
            if (!teamPlayerIds.contains(userId))
                throw new IllegalArgumentException("Player " + userId + " not in team");

        List<PlayerAvailability> existing =
                availabilityRepository.findByMatchAndTeam(match, team);

        for (PlayerAvailability a : existing) {
            a.setIsInPlayingXi(false);
            a.setIsCaptain(false);
            a.setIsWicketKeeper(false);
        }

        availabilityRepository.saveAll(existing);

        for (Long userId : selected) {

            User player = userRepository.getReferenceById(userId);

            PlayerAvailability availability =
                    availabilityRepository
                            .findByMatchAndTeamAndPlayer(match, team, player)
                            .orElseGet(() -> {
                                PlayerAvailability a = new PlayerAvailability();
                                a.setMatch(match);
                                a.setTeam(team);
                                a.setPlayer(player);
                                a.setCreatedAt(LocalDateTime.now());
                                return a;
                            });

            availability.setIsInPlayingXi(true);
            availability.setStatus(AvailabilityStatus.AVAILABLE);
            availability.setIsCaptain(userId.equals(req.getCaptainUserId()));
            availability.setIsWicketKeeper(userId.equals(req.getWicketKeeperUserId()));
            availability.setConfirmedAt(LocalDateTime.now());

            availabilityRepository.save(availability);
        }
    }

    private Team getTeamAndVerify(Long managerUserId, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!managerUserId.equals(team.getManagerUserId()))
            throw new IllegalStateException("Access denied");

        return team;
    }

    private Match validateMatchBelongsToTeam(Long matchId, Long teamId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!match.getTeamAId().equals(teamId)
                && !match.getTeamBId().equals(teamId))
            throw new IllegalStateException("Match does not belong to this team");

        return match;
    }
}
