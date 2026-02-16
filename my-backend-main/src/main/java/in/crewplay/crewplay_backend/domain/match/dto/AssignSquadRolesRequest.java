package in.crewplay.crewplay_backend.domain.match.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignSquadRolesRequest {

    @NotNull
    private Long teamId;

    @NotEmpty
    private List<SquadRoleDTO> roles;
}
