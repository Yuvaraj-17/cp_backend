package in.crewplay.crewplay_backend.domain.match.verification;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.MatchTeam;
import in.crewplay.crewplay_backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_verifications")
@Getter
@Setter
public class MatchVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which match
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    // Team side (A / B)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_team_id", nullable = false)
    private MatchTeam matchTeam;

    // Selected verifier (must be registered user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verifier_user_id", nullable = false)
    private User verifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchVerificationStatus status = MatchVerificationStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
}
