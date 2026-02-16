package in.crewplay.crewplay_backend.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Entity
@Table(name = "scorer_profiles")
@Getter
@Setter
public class ScorerProfile {

    @Id
    private Long id;

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

    // SOCIAL (COUNTS ONLY)
    @Column(name = "followers_count")
    private Long followersCount = 0L;

    @Column(name = "following_count")
    private Long followingCount = 0L;

    // SCORER TRUST SIGNAL
    @Column(name = "unverified_matches_count")
    private Long unverifiedMatchesCount = 0L;

    // META
    @Column(name = "member_since")
    private LocalDateTime memberSince;

    private Integer matchesScored = 0;
    private Double accuracy = 100.0;

    // SYSTEM
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
