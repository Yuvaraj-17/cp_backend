package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Full squad overview for a match.
 */
@Getter
@Builder
public class SquadManagementResponse {

    private Long matchId;
    private String matchTitle;

    private Integer playingXiSelected;
    private Integer playingXiTotal;
    private Integer totalSquad;

    private Integer available;
    private Integer unavailable;
    private Integer pending;

    private SquadPlayerCard captain;
    private SquadPlayerCard wicketKeeper;

    private List<SquadPlayerCard> players;

    private Boolean isXiConfirmed;
}
