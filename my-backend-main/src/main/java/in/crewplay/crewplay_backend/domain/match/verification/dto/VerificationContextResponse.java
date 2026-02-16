package in.crewplay.crewplay_backend.domain.match.verification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationContextResponse {

    private VerificationTeamDTO teamA;
    private VerificationTeamDTO teamB;
}