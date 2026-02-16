package in.crewplay.crewplay_backend.team_manager.Service;

import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.domain.user.TeamManagerProfile;
import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.domain.user.repository.UserRepository;
import in.crewplay.crewplay_backend.team.repository.TeamRepository;
import in.crewplay.crewplay_backend.team_manager.Repository.TeamManagerProfileRepository;
import in.crewplay.crewplay_backend.team_manager.dto.response.TeamManagerProfileResponse;
import in.crewplay.crewplay_backend.team_manager.dto.request.UpdateManagerProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * service handling Team Manager profile lifecycle and stats.
 * Responsible for:
 * - Auto profile creation
 * - Profile updates
 * - Career stats tracking
 * - Active team switching
 */
@Service
@RequiredArgsConstructor
public class TeamManagerProfileService {

    private final TeamManagerProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    /**
     * Creates manager profile if not already present.
     * Uses @MapsId (profile.id == user.id).
     */
    @Transactional
    public TeamManagerProfile getOrCreateProfile(Long userId) {

        return profileRepository.findById(userId).orElseGet(() -> {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            TeamManagerProfile profile = new TeamManagerProfile();
            profile.setUser(user); // @MapsId sets ID automatically

            // Safe default name
            String defaultName = (user.getEmail() != null && user.getEmail().contains("@"))
                    ? user.getEmail().split("@")[0]
                    : "Manager";

            profile.setName(defaultName);
            profile.setEmail(user.getEmail());
            profile.setMobileNumber(user.getMobileNumber());

            profile.setMemberSince(LocalDateTime.now());
            profile.setCreatedAt(LocalDateTime.now());

            // Default permissions
            profile.setCanScheduleMatches(true);
            profile.setCanRecruitPlayers(true);
            profile.setCanManageFinances(false);
            profile.setIsVerifiedManager(false);

            return profileRepository.save(profile);
        });
    }

    /**
     * Builds full profile response (Screen 16).
     * Derived fields like winRate are computed dynamically.
     */
    public TeamManagerProfileResponse buildProfileResponse(Long userId) {

        TeamManagerProfile p = getOrCreateProfile(userId);

        double winRate = 0.0;
        if (p.getTotalMatchesManaged() > 0) {
            winRate = ((double) p.getTotalWins() / p.getTotalMatchesManaged()) * 100;
        }

        String memberSince = (p.getMemberSince() != null)
                ? p.getMemberSince().format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                : "Unknown";

        return TeamManagerProfileResponse.builder()
                .userId(userId)
                .name(p.getName())
                .email(p.getEmail())
                .mobileNumber(p.getMobileNumber())
                .city(p.getCity())
                .profileImageUrl(p.getProfileImageUrl())
                .followersCount(p.getFollowersCount())
                .followingCount(p.getFollowingCount())
                .totalSeasons(p.getTotalSeasons())
                .totalMatchesManaged(p.getTotalMatchesManaged())
                .totalWins(p.getTotalWins())
                .totalLosses(p.getTotalLosses())
                .winRate(round(winRate))
                .formRating(p.getFormRating())
                .managerRank(p.getManagerRank())
                .activeTeamId(p.getActiveTeamId())
                .canScheduleMatches(p.getCanScheduleMatches())
                .canRecruitPlayers(p.getCanRecruitPlayers())
                .canManageFinances(p.getCanManageFinances())
                .isVerifiedManager(p.getIsVerifiedManager())
                .memberSince(memberSince)
                .teamsHandledCount(p.getTeamsHandledCount())
                .build();
    }

    /**
     * Updates editable profile fields.
     */
    @Transactional
    public TeamManagerProfile updateProfile(Long userId,
                                            UpdateManagerProfileRequest req) {

        TeamManagerProfile profile = getOrCreateProfile(userId);

        if (req.getName() != null) profile.setName(req.getName());
        if (req.getProfileImageUrl() != null) profile.setProfileImageUrl(req.getProfileImageUrl());
        if (req.getCity() != null) profile.setCity(req.getCity());
        if (req.getMobileNumber() != null) profile.setMobileNumber(req.getMobileNumber());

        profile.setUpdatedAt(LocalDateTime.now());
        return profileRepository.save(profile);
    }

    /**
     * Updates manager career stats after match completion.
     * Should be triggered from match-completion hook.
     */
    @Transactional
    public void recordMatchResult(Long managerUserId,
                                  boolean won,
                                  boolean lost) {

        TeamManagerProfile profile = getOrCreateProfile(managerUserId);

        profile.setTotalMatchesManaged(profile.getTotalMatchesManaged() + 1);

        if (won) profile.setTotalWins(profile.getTotalWins() + 1);
        if (lost) profile.setTotalLosses(profile.getTotalLosses() + 1);

        profile.setUpdatedAt(LocalDateTime.now());
        profileRepository.save(profile);
    }

    /**
     * Switches active team.
     * Validates ownership before switching.
     */
    @Transactional
    public void switchActiveTeam(Long userId, Long teamId) {

        TeamManagerProfile profile = getOrCreateProfile(userId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!userId.equals(team.getManagerUserId())) {
            throw new IllegalStateException("You do not manage this team");
        }

        profile.setActiveTeamId(teamId);
        profile.setUpdatedAt(LocalDateTime.now());
        profileRepository.save(profile);
    }

    /**
     * Soft delete account (deactivates user).
     * Historical data remains for audit integrity.
     */
    @Transactional
    public void deleteAccount(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(false);
        userRepository.save(user);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
