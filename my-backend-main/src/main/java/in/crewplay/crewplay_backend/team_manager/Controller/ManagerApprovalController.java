package in.crewplay.crewplay_backend.team_manager.Controller;

import in.crewplay.crewplay_backend.team_manager.Service.ManagerApprovalService;
import in.crewplay.crewplay_backend.team_manager.dto.response.ApprovalsOverviewResponse;
import in.crewplay.crewplay_backend.team_manager.dto.response.PendingApprovalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles approval actions for join requests.
 */
@RestController
@RequestMapping("/team-manager/teams/{teamId}/approvals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEAM_MANAGER')")
public class ManagerApprovalController {

    private final ManagerApprovalService approvalService;

    @GetMapping
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<ApprovalsOverviewResponse> getApprovals(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId) {

        return ResponseEntity.ok(
                approvalService.getApprovals(userId, teamId)
        );
    }

    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<Void> approve(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId,
            @PathVariable Long requestId) {

        approvalService.approveRequest(userId, teamId, requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<Void> reject(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId,
            @PathVariable Long requestId) {

        approvalService.rejectRequest(userId, teamId, requestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<List<PendingApprovalResponse>> getHistory(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long teamId) {

        return ResponseEntity.ok(
                approvalService.getHistory(userId, teamId)
        );
    }
}
