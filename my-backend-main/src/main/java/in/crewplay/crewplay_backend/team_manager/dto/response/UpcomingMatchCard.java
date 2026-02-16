package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpcomingMatchCard {
    private Long   matchId;
    private String homeTeamName;
    private String opponentTeamName;
    private String matchDateTime;     // "Mon 17 Feb, 4:00 PM"
    private String venue;
    private String matchType;         // "T20", "ODI", "FRIENDLY"
    private String matchStatus;       // "DRAFT", "READY", "VERIFIED"
    private boolean squadSubmitted;
    private String submissionDeadline; // "3h remaining" / "Deadline passed"
}