package in.crewplay.crewplay_backend.team_manager.dto.request;

import in.crewplay.crewplay_backend.domain.teams.enums.AvailabilityStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Used when a player confirms availability for a match.
 */
@Getter
@Setter
public class UpdateAvailabilityRequest {

    private Long matchId;
    private AvailabilityStatus status;
}
