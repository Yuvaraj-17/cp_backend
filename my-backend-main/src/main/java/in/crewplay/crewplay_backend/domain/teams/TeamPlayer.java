package in.crewplay.crewplay_backend.domain.teams;

import in.crewplay.crewplay_backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_players",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"team_id", "user_id"})
        })
@Getter
@Setter
public class TeamPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();
}
