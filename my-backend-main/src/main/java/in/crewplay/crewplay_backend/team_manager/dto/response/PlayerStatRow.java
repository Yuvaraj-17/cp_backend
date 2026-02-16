package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents one player row in team stats.
 */
@Getter
@Builder
public class PlayerStatRow {

    private Long userId;
    private Integer runs;
}
