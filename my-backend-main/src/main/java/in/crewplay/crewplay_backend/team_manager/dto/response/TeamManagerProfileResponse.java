package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Sent to frontend when manager opens Profile screen.
 */
@Getter
@Builder
public class TeamManagerProfileResponse {

    private Long userId;
    private String name;
    private String email;
    private String mobileNumber;
    private String city;
    private String profileImageUrl;

    private Long followersCount;
    private Long followingCount;

    private Integer totalSeasons;
    private Integer totalMatchesManaged;
    private Integer totalWins;
    private Integer totalLosses;
    private Double winRate;

    private Double formRating;
    private Integer managerRank;

    private Long activeTeamId;
    private Boolean canScheduleMatches;
    private Boolean canRecruitPlayers;
    private Boolean canManageFinances;
    private Boolean isVerifiedManager;

    private String memberSince;
    private Long teamsHandledCount;
}
