package in.crewplay.crewplay_backend.domain.match;

public enum MatchStatus {


    DRAFT,                   // Squads not finalized
    READY,                   // Squads finalized
    SPECS_LOCKED,            // Match specs saved
    AWAITING_VERIFICATION,   // Waiting for team confirmations
    VERIFIED,                // Both teams confirmed
    TOSS_DONE,               // Toss completed
    LIVE,                    // Innings started
    COMPLETED ;               // Match finished

    public boolean isPreMatch() {
        return this == DRAFT ||
                this == READY ||
                this == SPECS_LOCKED ||
                this == AWAITING_VERIFICATION ||
                this == VERIFIED;
    }

    public boolean isInProgress() {
        return this == LIVE;
    }

    public boolean isFinished() {
        return this == COMPLETED;
    }
    }


