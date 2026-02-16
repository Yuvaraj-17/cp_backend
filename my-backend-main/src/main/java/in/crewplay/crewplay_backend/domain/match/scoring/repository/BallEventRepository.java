package in.crewplay.crewplay_backend.domain.match.scoring.repository;

import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.innings.MatchInnings;
import in.crewplay.crewplay_backend.domain.match.scoring.entity.BallEvent;
import in.crewplay.crewplay_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BallEventRepository
        extends JpaRepository<BallEvent, Long> {

    // Last 6 balls (excluding undone)
    List<BallEvent> findTop6ByInningsAndIsUndoneFalseOrderByIdDesc(
            MatchInnings innings
    );

    // Last valid ball (excluding undone)
    BallEvent findTopByInningsAndIsUndoneFalseOrderByIdDesc(
            MatchInnings innings
    );

    // Last undone ball (for redo)
    BallEvent findTopByInningsAndIsUndoneTrueOrderByIdDesc(
            MatchInnings innings
    );

    // ---------------- Batsman Stats ----------------

    @Query("""
        SELECT COALESCE(SUM(b.runsOffBat), 0)
        FROM BallEvent b
        WHERE b.batsman.id = :playerId
        AND b.innings.id = :inningsId
        AND b.isUndone = false
    """)
    int sumRunsByBatsman(
            @Param("playerId") Long playerId,
            @Param("inningsId") Long inningsId
    );

    @Query("""
        SELECT COUNT(b)
        FROM BallEvent b
        WHERE b.batsman.id = :playerId
        AND b.innings.id = :inningsId
        AND b.ballResultType = 'NORMAL'
        AND b.isUndone = false
    """)
    int countBallsByBatsman(
            @Param("playerId") Long playerId,
            @Param("inningsId") Long inningsId
    );

    // ---------------- Bowler Stats ----------------

    @Query("""
        SELECT COALESCE(SUM(b.runsOffBat + b.extraRuns), 0)
        FROM BallEvent b
        WHERE b.bowler.id = :bowlerId
        AND b.innings.id = :inningsId
        AND b.isUndone = false
    """)
    int sumRunsByBowler(
            @Param("bowlerId") Long bowlerId,
            @Param("inningsId") Long inningsId
    );

    @Query("""
        SELECT COUNT(b)
        FROM BallEvent b
        WHERE b.bowler.id = :bowlerId
        AND b.innings.id = :inningsId
        AND b.ballResultType = 'NORMAL'
        AND b.isUndone = false
    """)
    int countBallsByBowler(
            @Param("bowlerId") Long bowlerId,
            @Param("inningsId") Long inningsId
    );

    @Query("""
    SELECT COUNT(b)
    FROM BallEvent b
    WHERE b.bowler.id = :bowlerId
    AND b.innings.id = :inningsId
    AND b.isWicket = true
    AND b.wicketType <> 'RUN_OUT'
    AND b.wicketType <> 'RETIRED_HURT'
""")
    int countWicketsByBowler(
            @Param("bowlerId") Long bowlerId,
            @Param("inningsId") Long inningsId
    );

    @Query("""
SELECT b
FROM BallEvent b
WHERE b.innings = :innings
AND b.overNumber = :overNumber
AND b.isUndone = false
ORDER BY b.id ASC
""")
    List<BallEvent> findLastOverBalls(
            @Param("innings") MatchInnings innings,
            @Param("overNumber") int overNumber
    );
    /**
     * Fetch all ball events for a specific match.
     * Used for match analytics, phase breakdown, score calculation.
     */
    List<BallEvent> findByMatch(Match match);

    /**
     * Fetch all balls faced by a batsman across matches.
     * Used for career-level batting statistics.
     */
    List<BallEvent> findByBatsman(User batsman);

    /**
     * Fetch all balls bowled by a bowler across matches.
     * Used for career-level bowling statistics.
     */
    List<BallEvent> findByBowler(User bowler);

    /**
     * Fetch balls faced by a batsman in a specific match.
     * Used for match-specific performance stats.
     */
    List<BallEvent> findByMatchAndBatsman(Match match, User batsman);

    /**
     * Fetch balls bowled by a bowler in a specific match.
     * Used for match-specific bowling analytics.
     */
    List<BallEvent> findByMatchAndBowler(Match match, User bowler);

    List<BallEvent> findByMatch_IdIn(List<Long> matchIds);


}