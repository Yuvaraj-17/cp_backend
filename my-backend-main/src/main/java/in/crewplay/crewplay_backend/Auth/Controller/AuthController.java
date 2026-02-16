package in.crewplay.crewplay_backend.Auth.Controller;


import in.crewplay.crewplay_backend.Auth.DTO.Request.RequestOtpRequest;
import in.crewplay.crewplay_backend.Auth.DTO.Request.StartJourneyRequest;
import in.crewplay.crewplay_backend.Auth.DTO.Request.VerifyOtpRequest;
import in.crewplay.crewplay_backend.Auth.service.EmailService;
import in.crewplay.crewplay_backend.Auth.util.JwtUtil;
import in.crewplay.crewplay_backend.Auth.service.OtpService;
import in.crewplay.crewplay_backend.domain.user.repository.UserRepository;
import in.crewplay.crewplay_backend.domain.user.repository.UserRoleRepository;
import in.crewplay.crewplay_backend.domain.user.Role;
import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.domain.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final OtpService otpService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtUtil jwtUtil;

    public AuthController(
            OtpService otpService,
            EmailService emailService,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            JwtUtil jwtUtil
    ) {
        this.otpService = otpService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * STEP 1 — REQUEST OTP (Signup + Login)
     */
    @PostMapping("/request-otp")
    public void requestOtp(@Valid @RequestBody RequestOtpRequest request) {
        String otp = otpService.generateAndStoreOtp(request.getEmail());
        emailService.sendOtpEmail(request.getEmail(), otp);
    }

    /**
     * STEP 2 — VERIFY OTP (Identity Proven)
     */
    @PostMapping("/verify-otp")
    public Long verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {

        boolean valid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(request.getEmail());
                    return userRepository.save(newUser);
                });

        // Frontend only needs userId at this stage
        return user.getId();
    }

    /**
     * STEP 3 — START JOURNEY (ONE ACTIVE ROLE GUARANTEED)
     */
    @PostMapping("/start-journey/{userId}")
    public String startJourney(
            @PathVariable Long userId,
            @Valid @RequestBody StartJourneyRequest request
    ) {
        Role selectedRole = request.getRole();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1️⃣ Deactivate ALL existing roles
        List<UserRole> existingRoles = userRoleRepository.findByUserId(userId);
        existingRoles.forEach(ur -> ur.setIsActive(false));
        userRoleRepository.saveAll(existingRoles);

        // 2️⃣ Activate or create the selected role
        UserRole currentRole = userRoleRepository
                .findByUserIdAndRole(userId, selectedRole)
                .orElseGet(() -> {
                    UserRole ur = new UserRole();
                    ur.setUser(user);
                    ur.setRole(selectedRole);
                    return ur;
                });

        currentRole.setIsActive(true);
        userRoleRepository.save(currentRole);

        // 3️⃣ Collect ALL roles owned by user (for JWT)
        List<String> roleNames = userRoleRepository.findByUserId(userId)
                .stream()
                .map(ur -> ur.getRole().name())
                .toList();

        // 4️⃣ Issue JWT with ACTIVE ROLE
        return jwtUtil.generateToken(
                userId,
                roleNames,
                selectedRole.name()
        );
    }
}
