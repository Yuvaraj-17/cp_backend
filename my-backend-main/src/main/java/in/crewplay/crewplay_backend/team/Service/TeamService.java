package in.crewplay.crewplay_backend.team.Service;

import in.crewplay.crewplay_backend.Auth.service.OtpService;
import in.crewplay.crewplay_backend.common.enums.PlayerAddMethod;
import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.domain.teams.enums.TeamStatus;
import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.domain.user.repository.UserRepository;
import in.crewplay.crewplay_backend.team.repository.TeamRepository;
import in.crewplay.crewplay_backend.team.util.TeamCodeGenerator;
import in.crewplay.crewplay_backend.team_roster.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final OtpService otpService;
    private final TeamCodeGenerator teamCodeGenerator;

    /**
     * STEP 1 — Send captain verification OTP (EMAIL)
     */
    public void sendCaptainVerificationOtp(String captainEmail) {
        otpService.generateAndStoreOtp(captainEmail);
    }

    /**
     * STEP 2 — Create Team (OTP already verified)
     *
     * IMPORTANT:
     * - Team is created as TEMPORARY
     * - TEAM_MANAGER role is NOT assigned here
     * - Ownership will be granted after match completion
     */
    @Transactional
    public Team createTeam(
            Long scorerUserId,
            String teamName,
            String city,
            String logoUrl,
            String captainMobileNumber
    ) {

        // 1️⃣ Prevent duplicate team in same city
        if (teamRepository.existsByNameAndCity(teamName, city)) {
            throw new RuntimeException("Team already exists in this city");
        }

        // 2️⃣ Captain must be an existing user
        User captain = userRepository.findByMobileNumber(captainMobileNumber)
                .orElseThrow(() -> new RuntimeException("Captain must be an existing user"));

        // 3️⃣ Generate team code
        String teamCode = teamCodeGenerator.generate();

        // 4️⃣ Create TEMPORARY team
        Team team = new Team();
        team.setName(teamName);
        team.setCity(city);
        team.setLogoUrl(logoUrl);
        team.setTeamCode(teamCode);
        team.setCaptain(captain);
        team.setCreatedByScorerId(scorerUserId);
        team.setVerificationMethod("EMAIL");
        team.setStatus(TeamStatus.TEMPORARY); // 🔥 CRITICAL CHANGE
        team.setCreatedAt(LocalDateTime.now());

        Team savedTeam = teamRepository.save(team);

        // 5️⃣ Add captain to roster
        TeamMember captainMember = new TeamMember();
        captainMember.setTeam(savedTeam);
        captainMember.setUser(captain);
        captainMember.setDisplayName(extractDisplayName(captain));
        captainMember.setMobileNumber(captainMobileNumber);
        captainMember.setAddMethod(PlayerAddMethod.TEAM_CODE);
        captainMember.setIsGuest(false);
        captainMember.setReported(false);
        captainMember.setAddedByScorerId(scorerUserId);
        captainMember.setCreatedAt(LocalDateTime.now());

        teamMemberRepository.save(captainMember);

        // ❌ REMOVED:
        // assignTeamManagerRoleIfMissing(captain);

        return savedTeam;
    }

    /**
     * Fallback display name logic (Phase-0 safe)
     */
    private String extractDisplayName(User user) {
        if (user.getEmail() != null) {
            return user.getEmail().split("@")[0];
        }
        return user.getMobileNumber();
    }
}
