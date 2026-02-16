package in.crewplay.crewplay_backend.domain.match.dto;

import in.crewplay.crewplay_backend.domain.match.dto.SquadPlayerDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubmitSquadRequest {

    @NotNull
    private Long teamAId;

    @NotNull
    private Long teamBId;

    @NotEmpty
    private List<SquadPlayerDTO> teamA;

    @NotEmpty
    private List<SquadPlayerDTO> teamB;

    // Optional in Phase-0
    private String city;
}
