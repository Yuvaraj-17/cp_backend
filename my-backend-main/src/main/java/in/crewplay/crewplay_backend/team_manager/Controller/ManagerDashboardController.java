package in.crewplay.crewplay_backend.team_manager.Controller;

import in.crewplay.crewplay_backend.team_manager.Service.ManagerDashboardService;
import in.crewplay.crewplay_backend.team_manager.dto.response.ManagerDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/team-manager/dashboard")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final ManagerDashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<ManagerDashboardResponse> getDashboard(
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(dashboardService.buildDashboard(userId));
    }
}