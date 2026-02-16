package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;


/**
 * Contains approval stats + list of pending requests.
 */
@Getter
@Builder
public class ApprovalsOverviewResponse {

    private Integer totalPending;
    private Integer newToday;
    private Integer expiringSoon;

    private List<PendingApprovalResponse> requests;
}
