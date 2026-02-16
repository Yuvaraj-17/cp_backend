package in.crewplay.crewplay_backend.domain.match;

import in.crewplay.crewplay_backend.domain.teams.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "match_teams",
        uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "side"}))
@Getter
@Setter
public class MatchTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // "A" or "B"
    @Column(nullable = false)
    private String side;
}
