package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents each player in roster screen.
 */
@Getter
@Builder
public class RosterPlayerResponse {

    private Long memberId;
    private Long userId;

    private String displayName;
    private String mobileNumber;

    private String addMethod;
    private Boolean isGuest;

    private String verificationStatus;  // Guest / Verified / Pending
}
