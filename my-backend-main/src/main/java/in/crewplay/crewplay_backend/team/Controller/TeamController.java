package in.crewplay.crewplay_backend.team.Controller;


import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.team.Service.TeamService;
import in.crewplay.crewplay_backend.team.dto.request.CreateTeamRequest;
import in.crewplay.crewplay_backend.team.dto.request.VerifyCaptainRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    /**
     * STEP 1 — Send captain verification OTP (EMAIL)
     */
    @PostMapping("/verify-captain")
    @PreAuthorize("hasRole('SCORER')")
    public ResponseEntity<String> verifyCaptain(
            @RequestBody VerifyCaptainRequest request
    ) {
        teamService.sendCaptainVerificationOtp(request.getCaptainEmail());
        return ResponseEntity.ok("OTP sent to captain email");
    }

    /**
     * STEP 2 — Create team (OTP already verified on frontend)
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('SCORER')")
    public ResponseEntity<Team> createTeam(
            @RequestAttribute("userId") Long scorerUserId,
            @RequestBody CreateTeamRequest request
    ) {
        Team team = teamService.createTeam(
                scorerUserId,
                request.getTeamName(),
                request.getCity(),
                request.getLogoUrl(),
                request.getCaptainMobileNumber()
        );
        return ResponseEntity.ok(team);
    }
}
