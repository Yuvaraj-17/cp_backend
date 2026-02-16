package in.crewplay.crewplay_backend.domain.match.scoring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerScore {

    private Long userId;
    private String name;

    private int runs;
    private int balls;
    private double strikeRate;
}
