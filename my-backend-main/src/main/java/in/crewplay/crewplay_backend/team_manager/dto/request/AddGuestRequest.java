package in.crewplay.crewplay_backend.team_manager.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Used when manager adds a manual guest player.
 */
@Getter
@Setter
public class AddGuestRequest {

    private String displayName;
    private String mobileNumber;
}
