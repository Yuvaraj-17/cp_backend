package in.crewplay.crewplay_backend.team_manager.Controller;

import in.crewplay.crewplay_backend.domain.user.TeamManagerProfile;
import in.crewplay.crewplay_backend.team_manager.dto.request.UpdateManagerProfileRequest;
import in.crewplay.crewplay_backend.team_manager.dto.response.TeamManagerProfileResponse;
import in.crewplay.crewplay_backend.team_manager.Service.TeamManagerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Handles Team Manager profile operations.
 */
@RestController
@RequestMapping("/team-manager")
@RequiredArgsConstructor

@PreAuthorize("hasRole('TEAM_MANAGER')")
public class TeamManagerProfileController {

    private final TeamManagerProfileService profileService;

    /**
     * GET Profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<TeamManagerProfileResponse> getMyProfile(
            @RequestAttribute("userId") Long userId) {

        return ResponseEntity.ok(
                profileService.buildProfileResponse(userId)
        );
    }

    /**
     * UPDATE Profile
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<TeamManagerProfile> updateProfile(
            @RequestAttribute("userId") Long userId,
            @RequestBody UpdateManagerProfileRequest request) {

        return ResponseEntity.ok(
                profileService.updateProfile(userId, request)
        );
    }

    /**
     * DELETE Account (Soft Delete)
     */
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<Void> deleteAccount(
            @RequestAttribute("userId") Long userId) {

        profileService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }
}
