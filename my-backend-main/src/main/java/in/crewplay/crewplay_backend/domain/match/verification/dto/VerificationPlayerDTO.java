package in.crewplay.crewplay_backend.domain.match.verification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationPlayerDTO {

    private Long userId;
    private String name;
    private boolean isGuest;
    private boolean isScorer;
}