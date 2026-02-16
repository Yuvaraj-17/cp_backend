package in.crewplay.crewplay_backend.team_manager.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 📁 src/main/java/in/crewplay/crewplay_backend/team_manager/dto/response/DiscoverPlayerResponse.java
 * Action: CREATE
 *
 * Represents one player card on the Recruit / Discover screen.
 *
 * OVR rating formula (50–99):
 *   Batting (max 60):  min(20, runs/20) + min(20, avg*0.4) + min(20, sr/8)
 *   Bowling (max 40):  min(30, wickets*2.5) + max(0, 10 - economy)
 *   OVR = max(50, min(99, batting + bowling))
 *   Floor of 50 so every registered player has a meaningful badge.
 */
@Getter
@Builder
public class DiscoverPlayerResponse {

    private Long   userId;
    private String name;           // from PlayerProfile.name or derived from email

    /** BATSMAN | BOWLER | ALL_ROUNDER | WICKET_KEEPER | PLAYER (no history) */
    private String playingRole;

    /**
     * OVR badge displayed on the card (50–99).
     * Higher = better all-round platform-wide performance.
     */
    private int overallRating;

    // ── Stats shown under the OVR badge ──────────────────────────────────────
    private int    totalMatches;
    private double battingAverage;
    private double strikeRate;
    private int    totalWickets;
    private double economy;

    /**
     * Current invite state for THIS manager's team:
     *   null      → no invite sent → show "Invite" button (active)
     *   "PENDING" → invite sent, awaiting player → show "Invited" label (disabled)
     */
    private String inviteStatus;
}