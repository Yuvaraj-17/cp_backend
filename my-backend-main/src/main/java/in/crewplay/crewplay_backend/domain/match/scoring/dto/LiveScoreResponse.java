package in.crewplay.crewplay_backend.domain.match.scoring.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LiveScoreResponse {

    private String location;

    private int totalRuns;
    private int totalWickets;

    private String oversDisplay;

    private Integer target;
    private Integer runsRequired;

    private Double requiredRunRate;
    private Double currentRunRate;

    private PlayerScore striker;
    private PlayerScore nonStriker;

    private BowlerScore currentBowler;

    private List<String> currentOverBalls;

    private boolean freeHit;


}
