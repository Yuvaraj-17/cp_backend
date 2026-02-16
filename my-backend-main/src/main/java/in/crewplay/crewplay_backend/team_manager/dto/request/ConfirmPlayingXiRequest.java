package in.crewplay.crewplay_backend.team_manager.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Used when manager confirms Playing XI.
 */
@Getter
@Setter
public class ConfirmPlayingXiRequest {

    private Long matchId;

    private List<Long> playerUserIds;

    private Long captainUserId;
    private Long wicketKeeperUserId;
}
