package in.crewplay.crewplay_backend.domain.match.verification.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VerificationTeamDTO {

    private Long matchTeamId;
    private String teamName;
    private String logoUrl;
    private List<VerificationPlayerDTO> players;
}
