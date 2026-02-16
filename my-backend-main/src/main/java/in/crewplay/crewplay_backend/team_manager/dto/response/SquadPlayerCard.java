package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents one player in squad view.
 */
@Getter
@Builder
public class SquadPlayerCard {

    private Long userId;
    private String displayName;

    private String availabilityStatus;

    private Boolean isInPlayingXi;
    private Boolean isCaptain;
    private Boolean isWicketKeeper;
}
