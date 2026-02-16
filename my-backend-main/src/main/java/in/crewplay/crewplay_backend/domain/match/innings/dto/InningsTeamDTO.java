package in.crewplay.crewplay_backend.domain.match.innings.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InningsTeamDTO {
    private Long teamId;
    private String teamName;
    private String logoUrl;
    private List<InningsPlayerDTO> players;
}
