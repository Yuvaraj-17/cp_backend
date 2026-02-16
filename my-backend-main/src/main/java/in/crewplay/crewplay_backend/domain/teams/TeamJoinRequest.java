package in.crewplay.crewplay_backend.domain.teams;
// Handles BOTH player-initiated join requests AND manager invitations

import in.crewplay.crewplay_backend.domain.teams.enums.JoinRequestType;
import in.crewplay.crewplay_backend.domain.teams.enums.JoinRequestStatus;


import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.team.enums.JoinRequestStatusForOverAllTeam;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_join_requests",
        uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "player_user_id", "status"}))
@Getter
@Setter
public class TeamJoinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id") private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_user_id") private User player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by_user_id") private User initiatedBy;

    @Enumerated(EnumType.STRING) private JoinRequestType type;
    @Enumerated(EnumType.STRING) private JoinRequestStatus status = JoinRequestStatus.PENDING;
    @Column(columnDefinition = "TEXT") private String message;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime respondedAt;
    private LocalDateTime expiresAt; // 7-day expiry for invites

}