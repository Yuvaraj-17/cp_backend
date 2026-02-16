package in.crewplay.crewplay_backend.team_manager.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Used when manager edits profile.
 * Only editable fields are included.
 */
@Getter
@Setter
public class UpdateManagerProfileRequest {

    private String name;
    private String profileImageUrl;
    private String city;
    private String mobileNumber;

}
