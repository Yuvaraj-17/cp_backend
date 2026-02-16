package in.crewplay.crewplay_backend.team_manager.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Used when manager invites a registered player by mobile.
 */
@Getter
@Setter
public class InvitePlayerRequest {

    private String mobileNumber;
    private String message;
}
