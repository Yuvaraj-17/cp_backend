package in.crewplay.crewplay_backend.team_manager.Service;

import in.crewplay.crewplay_backend.common.enums.PlayerAddMethod;
import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.domain.teams.TeamMember;
import in.crewplay.crewplay_backend.domain.teams.enums.TeamStatus;
import in.crewplay.crewplay_backend.domain.user.TeamManagerProfile;
import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.domain.user.repository.UserRepository;
import in.crewplay.crewplay_backend.team.repository.TeamRepository;
import in.crewplay.crewplay_backend.team_manager.Repository.TeamManagerProfileRepository;
import in.crewplay.crewplay_backend.team_manager.dto.request.CreateTeamManagerRequest;
import in.crewplay.crewplay_backend.team_manager.dto.request.UpdateTeamRequest;
import in.crewplay.crewplay_backend.team_manager.dto.response.TeamSummaryResponse;
import in.crewplay.crewplay_backend.team_roster.repository.TeamMemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ManagerTeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamManagerProfileRepository profileRepository;
    private final in.crewplay.crewplay_backend.team.util.TeamCodeGenerator teamCodeGenerator;

    /**
     * Manager creates a team (Manager Mode).
     * Team is ACTIVE immediately.
     */
    @Transactional
    public Team createTeam(Long managerUserId, CreateTeamManagerRequest request) {

        if (request.getTeamName() == null || request.getTeamName().isBlank())
            throw new IllegalArgumentException("Team name is required");

        if (request.getCity() == null || request.getCity().isBlank())
            throw new IllegalArgumentException("City is required");

        if (teamRepository.existsByNameAndCity(request.getTeamName(), request.getCity()))
            throw new IllegalStateException("Team already exists in " + request.getCity());

        User manager = userRepository.findById(managerUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User captain = resolveCaptain(request, manager);

        Team team = new Team();
        team.setName(request.getTeamName());
        team.setCity(request.getCity());
        team.setLogoUrl(request.getLogoUrl());
        team.setHomeGround(request.getHomeGround());
        team.setTeamCode(teamCodeGenerator.generate());
        team.setCaptain(captain);
        team.setManagerUserId(managerUserId);
        team.setCreatedByScorerId(managerUserId);
        team.setStatus(TeamStatus.ACTIVE);
        team.setCreatedAt(LocalDateTime.now());

        Team saved = teamRepository.save(team);

        addMemberToTeam(saved, manager, managerUserId);

        if (!captain.getId().equals(managerUserId))
            addMemberToTeam(saved, captain, managerUserId);

        // Update manager profile
        TeamManagerProfile profile = profileRepository.findById(managerUserId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setActiveTeamId(saved.getId());
        profile.setTeamsHandledCount(profile.getTeamsHandledCount() + 1);
        profile.setUpdatedAt(LocalDateTime.now());
        profileRepository.save(profile);

        return saved;
    }

    public List<TeamSummaryResponse> getMyTeams(Long managerUserId) {

        Long activeTeamId = profileRepository.findById(managerUserId)
                .map(TeamManagerProfile::getActiveTeamId)
                .orElse(null);

        return teamRepository.findByManagerUserId(managerUserId)
                .stream()
                .map(team -> buildTeamSummary(team, activeTeamId))
                .collect(Collectors.toList());
    }

    @Transactional
    public Team updateTeam(Long managerUserId, Long teamId, UpdateTeamRequest req) {

        Team team = getTeamAndVerifyOwner(managerUserId, teamId);

        if (req.getTeamName() != null) team.setName(req.getTeamName());
        if (req.getLogoUrl() != null) team.setLogoUrl(req.getLogoUrl());
        if (req.getHomeGround() != null) team.setHomeGround(req.getHomeGround());
        if (req.getActiveLeague() != null) team.setActiveLeague(req.getActiveLeague());

        return teamRepository.save(team);
    }

    @Transactional
    public void recordMatchResult(Long teamId, boolean won, boolean lost, boolean drawn) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (won) team.setWins(team.getWins() + 1);
        if (lost) team.setLosses(team.getLosses() + 1);
        if (drawn) team.setDraws(team.getDraws() + 1);

        teamRepository.save(team);
    }

    public Team getTeamAndVerifyOwner(Long managerUserId, Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!managerUserId.equals(team.getManagerUserId()))
            throw new IllegalStateException("Access denied");

        return team;
    }

    private User resolveCaptain(CreateTeamManagerRequest request, User manager) {

        if (request.isMakeManagerCaptain()) {
            return manager;
        }

        if (request.getCaptainMobileNumber() == null || request.getCaptainMobileNumber().isBlank())
            throw new IllegalArgumentException("Captain mobile required");

        return userRepository.findByMobileNumber(request.getCaptainMobileNumber())
                .orElseThrow(() -> new RuntimeException("Captain not found"));
    }

    private void addMemberToTeam(Team team, User user, Long addedBy) {

        if (teamMemberRepository.existsByTeamAndUser(team, user)) {

            TeamMember m = new TeamMember();
            m.setTeam(team);
            m.setUser(user);

            String displayName = (user.getEmail() != null && user.getEmail().contains("@"))
                    ? user.getEmail().split("@")[0]
                    : "Player";

            m.setDisplayName(displayName);
            m.setMobileNumber(user.getMobileNumber() != null ? user.getMobileNumber() : "");
            m.setAddMethod(PlayerAddMethod.TEAM_CODE);
            m.setAddedByScorerId(addedBy);
            m.setIsGuest(false);
            m.setCreatedAt(LocalDateTime.now());

            teamMemberRepository.save(m);
        }
    }

    private TeamSummaryResponse buildTeamSummary(Team t, Long activeTeamId) {

        int totalPlayers = Math.toIntExact(teamMemberRepository.countByTeam_Id(t.getId()));

        return TeamSummaryResponse.builder()
                .teamId(t.getId())
                .teamName(t.getName())
                .logoUrl(t.getLogoUrl())
                .homeGround(t.getHomeGround())
                .activeLeague(t.getActiveLeague())
                .status(t.getStatus().name())
                .wins(t.getWins())
                .losses(t.getLosses())
                .draws(t.getDraws())
                .isCurrentActive(t.getId().equals(activeTeamId))
                .teamCode(t.getTeamCode())
                .totalPlayers(totalPlayers)
                .build();
    }

    @Transactional
    public void switchActiveTeam(Long userId, Long teamId) {

        TeamManagerProfile profile = profileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        Team team = teamRepository
                .findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        profile.setActiveTeam(team);
        profileRepository.save(profile);
    }

}
