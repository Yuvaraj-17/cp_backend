package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents one join request in approvals screen.
 */
@Getter
@Builder
public class PendingApprovalResponse {

    private Long requestId;
    private Long playerId;
    private String playerName;

    private String requestType;

    private String createdAt;
    private String expiresAt;

    private Boolean isExpiringSoon;
}
