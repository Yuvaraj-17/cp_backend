package in.crewplay.crewplay_backend.domain.user;

import in.crewplay.crewplay_backend.domain.teams.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Entity
@Table(name = "team_manager_profiles")
@Getter
@Setter
public class TeamManagerProfile {

    @Id
    private Long id; // SAME AS users.id

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;

    // BASIC INFO
    private String name;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    private String city;

    private String email;

    @Column(name = "mobile_number")
    private String mobileNumber;

    // SOCIAL
    @Column(name = "followers_count")
    private Long followersCount = 0L;

    @Column(name = "following_count")
    private Long followingCount = 0L;

    // TEAM MANAGEMENT META
    @Column(name = "teams_handled_count")
    private Long teamsHandledCount = 0L;

    // META
    @Column(name = "member_since")
    private LocalDateTime memberSince;

    // SYSTEM
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---------- ACTIVE TEAM ----------

    @Column(name = "active_team_id")
    private Long activeTeamId;


// ---------- CAREER STATS ----------

    @Column(name = "total_seasons", nullable = false)
    private Integer totalSeasons = 0;

    @Column(name = "total_matches_managed", nullable = false)
    private Integer totalMatchesManaged = 0;

    @Column(name = "total_wins", nullable = false)
    private Integer totalWins = 0;

    @Column(name = "total_losses", nullable = false)
    private Integer totalLosses = 0;


// ---------- PERFORMANCE METRICS ----------

    @Column(name = "manager_rank")
    private Integer managerRank;

    @Column(name = "form_rating", nullable = false)
    private Double formRating = 0.0;


// ---------- PERMISSIONS ----------

    @Column(name = "can_schedule_matches", nullable = false)
    private Boolean canScheduleMatches = true;

    @Column(name = "can_recruit_players", nullable = false)
    private Boolean canRecruitPlayers = true;

    @Column(name = "can_manage_finances", nullable = false)
    private Boolean canManageFinances = false;

    @Column(name = "is_verified_manager", nullable = false)
    private Boolean isVerifiedManager = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_team_id")
    private Team activeTeam;


}
