package in.crewplay.crewplay_backend.domain.match.scoring.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EndOverResponse {
    private int overNumber;

    private List<String> ballsInOver;

    private int runsInOver;
    private int wicketsInOver;
    private String chaseEquation;
    private int extrasInOver;

    private int totalRuns;
    private int totalWickets;

    private double currentRunRate;

    private Integer target;
    private Integer runsRequired;
    private Integer ballsRemaining;
    private Double requiredRunRate;

    private BowlerScore bowlerSummary;

    private PlayerScore striker;
    private PlayerScore nonStriker;
}
