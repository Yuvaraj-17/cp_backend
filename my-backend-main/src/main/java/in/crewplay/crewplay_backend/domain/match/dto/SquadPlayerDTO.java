package in.crewplay.crewplay_backend.domain.match.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SquadPlayerDTO {

    @NotNull
    private Long userId;

    private boolean captain;
    private boolean wicketKeeper;
}
