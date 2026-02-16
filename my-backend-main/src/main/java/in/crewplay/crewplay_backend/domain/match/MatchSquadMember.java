package in.crewplay.crewplay_backend.domain.match;

import in.crewplay.crewplay_backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "match_squad_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"match_team_id", "user_id"})
)
@Getter
@Setter
public class MatchSquadMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_team_id", nullable = false)
    private MatchTeam matchTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_captain", nullable = false)
    private boolean captain = false;

    @Column(name = "is_wicket_keeper", nullable = false)
    private boolean wicketKeeper = false;
}
