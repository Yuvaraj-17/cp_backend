package in.crewplay.crewplay_backend.Auth.DTO.Request;

import in.crewplay.crewplay_backend.domain.user.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StartJourneyRequest {

    @NotNull(message = "Role must be selected")
    private Role role;
}
