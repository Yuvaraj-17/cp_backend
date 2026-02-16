package in.crewplay.crewplay_backend.domain.match.scoring.dto;

import in.crewplay.crewplay_backend.domain.match.scoring.enums.BallResultType;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.WicketType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class WicketRequest {
    private WicketType wicketType;

    private Long dismissedPlayerId;

    private Long fielderId;

    private Boolean isDirectHit;

    private Integer runsCompletedBeforeWicket;

    private BallResultType deliveryType;
}
