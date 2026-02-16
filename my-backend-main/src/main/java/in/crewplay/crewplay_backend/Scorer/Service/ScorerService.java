package in.crewplay.crewplay_backend.Scorer.Service;

import in.crewplay.crewplay_backend.Scorer.Repository.ScorerProfileRepository;
import in.crewplay.crewplay_backend.domain.user.ScorerProfile;
import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScorerService {

    private final ScorerProfileRepository scorerProfileRepository;
    private final UserRepository userRepository;

    public ScorerProfile getOrCreateScorerProfile(Long userId) {

        return scorerProfileRepository.findById(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    ScorerProfile scorerProfile = new ScorerProfile();
                    scorerProfile.setId(userId);
                    scorerProfile.setUser(user);

                    // Auto-copy from User
                    scorerProfile.setEmail(user.getEmail());
                    scorerProfile.setMobileNumber(user.getMobileNumber());
                    scorerProfile.setName(extractNameFromEmail(user.getEmail()));

                    scorerProfile.setMemberSince(LocalDateTime.now());
                    scorerProfile.setCreatedAt(LocalDateTime.now());

                    return scorerProfileRepository.save(scorerProfile);
                });
    }

    private String extractNameFromEmail(String email) {
        return email.split("@")[0];
    }
}
