package in.crewplay.crewplay_backend.domain.match.scoring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BowlerScore {

    private Long userId;
    private String name;

    private int overs;
    private int balls;
    private int runsConceded;
    private int wickets;

    private double economy;
}
