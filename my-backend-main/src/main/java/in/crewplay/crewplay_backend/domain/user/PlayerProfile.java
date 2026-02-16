package in.crewplay.crewplay_backend.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_profiles")
@Getter
@Setter
public class PlayerProfile {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;

    // BASIC INFO
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String email;

    @Column(name = "mobile_number")
    private String mobileNumber;

    // CRICKET INFO
    @Enumerated(EnumType.STRING)
    @Column(name = "playing_role")
    private PlayingRole playingRole;

    private String battingStyle;
    private String bowlingStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel;

    // LOCATION
    private String city;

    // MEDIA
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // SOCIAL (COUNTS ONLY)
    @Column(name = "followers_count")
    private Long followersCount = 0L;

    @Column(name = "following_count")
    private Long followingCount = 0L;

    // SYSTEM
    @Column(name = "is_profile_complete")
    private Boolean isProfileComplete = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
