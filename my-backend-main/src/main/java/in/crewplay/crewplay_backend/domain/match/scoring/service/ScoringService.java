package in.crewplay.crewplay_backend.domain.match.scoring.service;


import in.crewplay.crewplay_backend.domain.match.Match;
import in.crewplay.crewplay_backend.domain.match.MatchStatus;
import in.crewplay.crewplay_backend.domain.match.innings.InningsStatus;
import in.crewplay.crewplay_backend.domain.match.innings.MatchInnings;
import in.crewplay.crewplay_backend.domain.match.innings.repository.MatchInningsRepository;
import in.crewplay.crewplay_backend.domain.match.repository.MatchRepository;
import in.crewplay.crewplay_backend.domain.match.scoring.dto.WicketRequest;
import in.crewplay.crewplay_backend.domain.match.scoring.dto.*;
import in.crewplay.crewplay_backend.domain.match.scoring.entity.BallEvent;
import in.crewplay.crewplay_backend.domain.match.scoring.entity.FallOfWicket;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.BallResultType;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.ExtraType;
import in.crewplay.crewplay_backend.domain.match.scoring.enums.WicketType;
import in.crewplay.crewplay_backend.domain.match.scoring.repository.BallEventRepository;
import in.crewplay.crewplay_backend.domain.match.scoring.repository.FallOfWicketRepository;
import in.crewplay.crewplay_backend.domain.teams.repository.TeamPlayerRepository;
import in.crewplay.crewplay_backend.domain.user.User;
import in.crewplay.crewplay_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.lang.Math.round;

@Service
@RequiredArgsConstructor
public class ScoringService {

    private final MatchRepository matchRepository;
    private final MatchInningsRepository inningsRepository;
    private final BallEventRepository ballEventRepository;
    private final UserRepository userRepository;
    private final FallOfWicketRepository fallOfWicketRepository;
    private final TeamPlayerRepository teamPlayerRepository;


    @Transactional
    public ScoreBallResponse scoreBall(ScoreBallRequest request, Long scorerUserId) {




        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));



        if (!match.getScorerUserId().equals(scorerUserId)) {
            throw new IllegalStateException("Only scorer can score");
        }

        if (match.getStatus() != MatchStatus.LIVE) {
            throw new IllegalStateException("Match not live");
        }

        MatchInnings innings = inningsRepository.findByMatch(match)
                .orElseThrow(() -> new RuntimeException("Innings not found"));

        // ---------------- STATE VALIDATION ----------------

        if (innings.getStatus() == InningsStatus.COMPLETED) {
            throw new IllegalStateException("Innings already completed");
        }

        if (innings.getStatus() != InningsStatus.LIVE) {
            throw new IllegalStateException("Innings not in LIVE state");
        }

// Striker must exist
        if (innings.getStriker() == null) {
            throw new IllegalStateException("Striker not selected");
        }

// Bowler must exist
        if (innings.getCurrentBowler() == null) {
            throw new IllegalStateException("Bowler not selected");
        }

// Validate striker ID
        if (!innings.getStriker().getId().equals(request.getStrikerId())) {
            throw new IllegalStateException("Invalid striker");
        }

// Validate bowler ID
        if (!innings.getCurrentBowler().getId().equals(request.getBowlerId())) {
            throw new IllegalStateException("Invalid bowler");
        }

        // 🔒 Validate striker belongs to batting team
        if (!teamPlayerRepository.existsByTeamIdAndUserId(
                match.getBattingTeamId(),
                request.getStrikerId()
        )) {
            throw new IllegalStateException("Striker must belong to batting team");
        }

// 🔒 Validate non-striker belongs to batting team
        if (!teamPlayerRepository.existsByTeamIdAndUserId(
                match.getBattingTeamId(),
                innings.getNonStriker().getId()
        )) {
            throw new IllegalStateException("Non-striker must belong to batting team");
        }

// 🔒 Validate bowler belongs to bowling team
        if (!teamPlayerRepository.existsByTeamIdAndUserId(
                match.getBowlingTeamId(),
                request.getBowlerId()
        )) {
            throw new IllegalStateException("Bowler must belong to bowling team");
        }

        // ---------------- BASIC NUMERIC VALIDATION ----------------

        if (request.getRunsOffBat() < 0) {
            throw new IllegalStateException("Runs off bat cannot be negative");
        }

        if (request.getExtraRuns() < 0) {
            throw new IllegalStateException("Extra runs cannot be negative");
        }

// ---------------- WIDE VALIDATION ----------------

        if (request.getBallResultType() == BallResultType.WIDE) {

            if (request.getRunsOffBat() != 0) {
                throw new IllegalStateException("Runs off bat not allowed on wide");
            }

            if (request.getExtraRuns() < 1) {
                throw new IllegalStateException("Wide must have at least 1 extra run");
            }

            if (request.getWicketDetails() != null &&
                    request.getWicketDetails().getWicketType() != WicketType.RUN_OUT) {
                throw new IllegalStateException("Only run-out allowed on wide");
            }
        }

        // ---------------- BASIC RUN VALIDATION ----------------
        if (request.getRunsOffBat() < 0) {
            throw new IllegalStateException("Runs off bat cannot be negative");
        }

        if (request.getExtraRuns() < 0) {
            throw new IllegalStateException("Extra runs cannot be negative");
        }

// ---------------- WIDE RULES ----------------
        if (request.getBallResultType() == BallResultType.WIDE) {

            if (request.getRunsOffBat() > 0) {
                throw new IllegalStateException("Runs off bat not allowed on wide");
            }

            if (request.getExtraRuns() <= 0) {
                throw new IllegalStateException("Wide must have at least 1 extra run");
            }

            if (request.getExtraType() != null &&
                    request.getExtraType() != ExtraType.NONE) {
                throw new IllegalStateException("Wide cannot have bye or leg-bye");
            }
        }

// ---------------- NO BALL RULES ----------------
        if (request.getBallResultType() == BallResultType.NO_BALL) {

            if (request.getExtraRuns() < 1) {
                throw new IllegalStateException("No ball must have at least 1 extra run");
            }

            // If runs off bat > 0, it cannot be bye
            if (request.getRunsOffBat() > 0 &&
                    request.getExtraType() == ExtraType.BYE) {
                throw new IllegalStateException("Cannot have runs off bat and bye together");
            }

            // If runs off bat > 0, it cannot be leg-bye
            if (request.getRunsOffBat() > 0 &&
                    request.getExtraType() == ExtraType.LEG_BYE) {
                throw new IllegalStateException("Cannot have runs off bat and leg-bye together");
            }
        }

// ---------------- NORMAL DELIVERY RULES ----------------
        if (request.getBallResultType() == BallResultType.NORMAL) {

            if (request.getExtraType() == ExtraType.BYE ||
                    request.getExtraType() == ExtraType.LEG_BYE) {

                if (request.getRunsOffBat() > 0) {
                    throw new IllegalStateException(
                            "Cannot combine runs off bat with bye/leg-bye"
                    );
                }
            }
        }

// ---------------- DEAD BALL RULE ----------------
        if (request.getBallResultType() == BallResultType.DEAD_BALL) {

            if (request.getRunsOffBat() > 0 ||
                    request.getExtraRuns() > 0) {
                throw new IllegalStateException(
                        "Dead ball cannot have runs"
                );
            }

            if (request.isWicket()) {
                throw new IllegalStateException(
                        "Dead ball cannot have wicket"
                );
            }
        }

// ---------------- NO BALL VALIDATION ----------------

        if (request.getBallResultType() == BallResultType.NO_BALL) {

            if (request.getExtraRuns() < 1) {
                throw new IllegalStateException("No ball must have at least 1 extra run");
            }

            if (request.getWicketDetails() != null &&
                    request.getWicketDetails().getWicketType() != WicketType.RUN_OUT) {
                throw new IllegalStateException("Only run-out allowed on no-ball");
            }
        }

// ---------------- DEAD BALL VALIDATION ----------------

        if (request.getBallResultType() == BallResultType.DEAD_BALL) {

            if (request.getRunsOffBat() > 0 || request.getExtraRuns() > 0) {
                throw new IllegalStateException("Dead ball cannot have runs");
            }

            if (request.getWicketDetails() != null) {
                throw new IllegalStateException("Dead ball cannot have wicket");
            }
        }

// ---------------- RUN OUT LOGIC VALIDATION ----------------

        if (request.getWicketDetails() != null &&
                request.getWicketDetails().getWicketType() == WicketType.RUN_OUT) {

            Integer completedRuns = request.getWicketDetails().getRunsCompletedBeforeWicket();

            if (completedRuns == null) {
                throw new IllegalStateException("Run out must specify runsCompletedBeforeWicket");
            }

            if (completedRuns < 0) {
                throw new IllegalStateException("Completed runs cannot be negative");
            }

            if (completedRuns > request.getRunsOffBat()) {
                throw new IllegalStateException("Completed runs cannot exceed runsOffBat");
            }
        }

        BallEvent event = buildBallEvent(request, match, innings);

        if (request.getWicketDetails() != null) {
            processWicket(request.getWicketDetails(), event, innings, match);
        }

        boolean overCompleted = applyScoringLogic(event, innings, match);
        validateOverIntegrity(innings);

        ballEventRepository.save(event);
        inningsRepository.save(innings);
        matchRepository.save(match);

        ScoreBallResponse response = new ScoreBallResponse();
        response.setLiveScore(getLiveScore(match.getId()));

        if (overCompleted) {
            response.setEndOverResponse(buildEndOverResponse(match, innings));
        }

        return response;
    }

    private BallEvent buildBallEvent(
            ScoreBallRequest request,
            Match match,
            MatchInnings innings
    ) {

        BallEvent event = new BallEvent();

        event.setMatch(match);
        event.setInnings(innings);
        event.setBowler(userRepository.getReferenceById(request.getBowlerId()));
        event.setBatsman(userRepository.getReferenceById(request.getStrikerId()));

        event.setBallResultType(request.getBallResultType());
        event.setRunsOffBat(request.getRunsOffBat());
        event.setExtraRuns(request.getExtraRuns());
        event.setExtraType(request.getExtraType());
        event.setWicket(request.isWicket());
        event.setWicketType(request.getWicketType());

        if (request.getDismissedPlayerId() != null) {
            event.setDismissedPlayer(
                    userRepository.getReferenceById(request.getDismissedPlayerId())
            );
        }

        event.setOverNumber(innings.getCurrentOver());
        event.setBallNumber(innings.getCurrentBall());

        return event;
    }

    private boolean applyScoringLogic(
            BallEvent event,
            MatchInnings innings,
            Match match) {

        int runsThisBall;

// Retired hurt must not affect score
        if (event.getWicketType() == WicketType.RETIRED_HURT) {
            runsThisBall = 0;
        }
        else if (event.isWicket()
                && event.getWicketType() == WicketType.RUN_OUT
                && event.getRunsCompletedBeforeWicket() != null) {

            runsThisBall = event.getRunsCompletedBeforeWicket()
                    + event.getExtraRuns();
        }
        else {
            runsThisBall = event.getRunsOffBat()
                    + event.getExtraRuns();
        }

        if (event.isWicket()
                && event.getWicketType() == WicketType.RUN_OUT
                && event.getRunsCompletedBeforeWicket() != null) {

            // Run-out special case
            runsThisBall = event.getRunsCompletedBeforeWicket()
                    + event.getExtraRuns();

        } else {
            runsThisBall = event.getRunsOffBat()
                    + event.getExtraRuns();
        }

        innings.setTotalRuns(
                innings.getTotalRuns() + runsThisBall
        );

        boolean overCompleted = false;

        switch (event.getBallResultType()) {

            case NORMAL:
                innings.setCurrentBall(innings.getCurrentBall() + 1);
                innings.setTotalValidBalls(innings.getTotalValidBalls() + 1);

                int runsForStrike;

                if (event.isWicket()
                        && event.getWicketType() == WicketType.RUN_OUT
                        && event.getRunsCompletedBeforeWicket() != null) {

                    runsForStrike = event.getRunsCompletedBeforeWicket();
                } else {
                    runsForStrike = event.getRunsOffBat();
                }

                if (runsForStrike % 2 == 1) {
                    swapStrike(innings);
                }

                if (innings.getCurrentBall() == 6) {

                    boolean wicketFell = event.isWicket();

                    innings.setCurrentOver(innings.getCurrentOver() + 1);
                    innings.setCurrentBall(0);
                    swapStrike(innings);

                    if (wicketFell) {
                        // First wait for batsman
                        innings.setStatus(InningsStatus.AWAITING_NEW_BATSMAN);
                        innings.setAwaitingBowlerAfterBatsman(true);
                    } else {
                        innings.setStatus(InningsStatus.AWAITING_NEW_BOWLER);
                    }

                    overCompleted = true;
                }
                break;

            case WIDE:
                handleWide(event, innings);
                break;

            case NO_BALL:
                handleNoBall(event, innings);
                break;

            case DEAD_BALL:
                return false;
        }



        // ---------------- CHASE COMPLETION ----------------
        if (innings.getTarget() != null) {

            if (innings.getTotalRuns() >= innings.getTarget()) {

                innings.setStatus(InningsStatus.COMPLETED);
                match.setStatus(MatchStatus.COMPLETED);

                // Hard lock: prevent further over processing
                return false;
            }

            // Defensive: score must never exceed target + current ball runs
            int maxAllowed = innings.getTarget() + 6;
            // 6 is max possible in one legal ball

            if (innings.getTotalRuns() > maxAllowed) {
                throw new IllegalStateException("Score exceeds logical target bounds");
            }
        }

        // ---------------- MAX OVER COMPLETION ----------------
        if (innings.getTarget() == null &&
                innings.getCurrentOver() >= innings.getMaxOvers()) {

            completeInnings(innings, match);
        }

        if (event.getBallResultType() == BallResultType.NORMAL) {
            innings.setFreeHit(false);
        }

        return overCompleted;
    }
    private void swapStrike(MatchInnings innings) {

        User temp = innings.getStriker();
        innings.setStriker(innings.getNonStriker());
        innings.setNonStriker(temp);
    }

    private void processWicket(
            WicketRequest wicketRequest,
            BallEvent event,
            MatchInnings innings,
            Match match
    ) {

        event.setWicket(true);
        event.setWicketType(wicketRequest.getWicketType());

        Long strikerId = innings.getStriker().getId();
        Long nonStrikerId = innings.getNonStriker().getId();

        // ✅ Validate dismissed player
        if (!wicketRequest.getDismissedPlayerId().equals(strikerId)
                && !wicketRequest.getDismissedPlayerId().equals(nonStrikerId)) {
            throw new IllegalStateException("Invalid dismissed player");
        }

        User dismissed = userRepository.getReferenceById(
                wicketRequest.getDismissedPlayerId()
        );
        event.setDismissedPlayer(dismissed);

        if (wicketRequest.getFielderId() != null) {

            // 🔒 Validate fielder belongs to bowling team
            if (!teamPlayerRepository.existsByTeamIdAndUserId(
                    match.getBowlingTeamId(),
                    wicketRequest.getFielderId()
            )) {
                throw new IllegalStateException("Fielder must belong to bowling team");
            }

            event.setFielder(
                    userRepository.getReferenceById(
                            wicketRequest.getFielderId()
                    )
            );
        }

        event.setIsDirectHit(wicketRequest.getIsDirectHit());
        event.setRunsCompletedBeforeWicket(
                wicketRequest.getRunsCompletedBeforeWicket()
        );
        event.setDismissalDeliveryType(
                wicketRequest.getDeliveryType()
        );

        // 🔥 FREE HIT PROTECTION
        if (innings.isFreeHit()
                && wicketRequest.getWicketType() != WicketType.RUN_OUT) {
            event.setWicket(false);
            event.setWicketType(null);
            return;
        }

        // ✅ RUN OUT VALIDATION
        if (wicketRequest.getWicketType() == WicketType.RUN_OUT) {
            if (wicketRequest.getRunsCompletedBeforeWicket() != null) {

                if (wicketRequest.getRunsCompletedBeforeWicket()
                        != event.getRunsOffBat()) {

                    throw new IllegalStateException(
                            "Run out runs mismatch with runsOffBat"
                    );
                }
            }
        }

        // ---------------- RETIRED HURT ----------------
        if (wicketRequest.getWicketType() == WicketType.RETIRED_HURT) {

            // Retired hurt:
            // - Does NOT count as wicket
            // - Does NOT affect bowler stats
            // - Does NOT consume ball
            // - Just replace batsman

            event.setBallResultType(BallResultType.DEAD_BALL);
            event.setWicket(false);

            innings.setStatus(InningsStatus.AWAITING_NEW_BATSMAN);
            return;
        }

        // ---------------- NORMAL WICKET ----------------
        innings.setTotalWickets(
                innings.getTotalWickets() + 1
        );

        createFallOfWicket(event, innings);

// 🔥 ALL OUT CHECK MUST COME FIRST
        if (innings.getTotalWickets() >= 10) {

            // Immediately end innings
            completeInnings(innings, match);

            return; // STOP further state changes
        }

// Normal wicket flow (not all out)
        innings.setStatus(InningsStatus.AWAITING_NEW_BATSMAN);
    }


    private void handleWide(
            BallEvent event,
            MatchInnings innings
    ) {
        // Wide does NOT count as ball

        if (event.getExtraRuns() > 1) {
            if ((event.getExtraRuns() - 1) % 2 == 1) {
                swapStrike(innings);
            }
        }
    }

    private void handleNoBall(
            BallEvent event,
            MatchInnings innings
    ) {
        // No ball does NOT count as valid delivery

        int totalRunsThisBall =
                event.getRunsOffBat()
                        + event.getExtraRuns();

        // If run out happened,
        // strike rotation depends on runs completed
        if (event.isWicket()
                && event.getWicketType() == WicketType.RUN_OUT
                && event.getRunsCompletedBeforeWicket() != null) {

            int completedRuns = event.getRunsCompletedBeforeWicket();

            if (completedRuns % 2 == 1) {
                swapStrike(innings);
            }

        } else {
            // Normal no-ball case (no wicket)
            if (event.getRunsOffBat() % 2 == 1) {
                swapStrike(innings);
            }
        }

        // Activate free hit for next legal delivery
        innings.setFreeHit(true);
    }

    private void completeInnings(MatchInnings innings, Match match) {

        innings.setStatus(InningsStatus.COMPLETED);

        if (innings.getInningsNumber() == 1) {

            Long oldBatting = match.getBattingTeamId();
            Long oldBowling = match.getBowlingTeamId();

            match.setBattingTeamId(oldBowling);
            match.setBowlingTeamId(oldBatting);

            MatchInnings second = new MatchInnings();
            second.setMatch(match);
            second.setInningsNumber(2);
            second.setMaxOvers(match.getOvers());
            second.setTarget(innings.getTotalRuns() + 1);
            second.setStatus(InningsStatus.LIVE);

            inningsRepository.save(second);

        } else {

            match.setStatus(MatchStatus.COMPLETED);
        }
    }

    @Transactional
    public void replaceBatsman(
            Long matchId,
            Long newBatsmanId,
            Long scorerUserId
    ) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!match.getScorerUserId().equals(scorerUserId)) {
            throw new IllegalStateException("Only scorer allowed");
        }

        MatchInnings innings = inningsRepository.findByMatch(match)
                .orElseThrow(() -> new RuntimeException("Innings not found"));

        if (innings.getStatus() == InningsStatus.COMPLETED) {
            throw new IllegalStateException("Innings already completed");
        }

        if (innings.getStatus() != InningsStatus.AWAITING_NEW_BATSMAN) {
            throw new IllegalStateException("Not waiting for new batsman");
        }

        User newBatsman = userRepository.getReferenceById(newBatsmanId);
        innings.setStriker(newBatsman);

        if (innings.isAwaitingBowlerAfterBatsman()) {
            innings.setStatus(InningsStatus.AWAITING_NEW_BOWLER);
            innings.setAwaitingBowlerAfterBatsman(false);
        } else {
            innings.setStatus(InningsStatus.LIVE);
        }

        inningsRepository.save(innings);
    }

    @Transactional
    public void changeBowler(
            Long matchId,
            Long newBowlerId,
            Long scorerUserId
    ) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!match.getScorerUserId().equals(scorerUserId)) {
            throw new IllegalStateException("Only scorer allowed");
        }

        MatchInnings innings = inningsRepository.findByMatch(match)
                .orElseThrow(() -> new RuntimeException("Innings not found"));

        if (innings.getStatus() == InningsStatus.COMPLETED) {
            throw new IllegalStateException("Innings already completed");
        }

        if (innings.getStatus() != InningsStatus.AWAITING_NEW_BOWLER) {
            throw new IllegalStateException("Not waiting for bowler change");
        }

        User newBowler = userRepository.getReferenceById(newBowlerId);

        if (!teamPlayerRepository.existsByTeamIdAndUserId(
                match.getBowlingTeamId(),
                newBowlerId
        )) {
            throw new IllegalStateException("Bowler must belong to bowling team");
        }
         //  Prevent consecutive overs
        if (innings.getCurrentBowler() != null &&
                innings.getCurrentBowler().getId().equals(newBowlerId)) {
            throw new IllegalStateException("Bowler cannot bowl consecutive overs");
        }

        innings.setCurrentBowler(newBowler);
        innings.setStatus(InningsStatus.LIVE);

        inningsRepository.save(innings);
    }

    @Transactional(readOnly = true)
    public LiveScoreResponse getLiveScore(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        MatchInnings innings = inningsRepository.findByMatch(match)
                .orElseThrow(() -> new RuntimeException("Innings not found"));

        LiveScoreResponse response = new LiveScoreResponse();

        response.setLocation(match.getCity());
        response.setTotalRuns(innings.getTotalRuns());
        response.setTotalWickets(innings.getTotalWickets());

        response.setOversDisplay(
                innings.getCurrentOver() + "." + innings.getCurrentBall()
        );

        response.setTarget(innings.getTarget());
        response.setFreeHit(innings.isFreeHit());

        int totalBalls = innings.getTotalValidBalls();

        if (totalBalls > 0) {
            double runRate = (innings.getTotalRuns() * 6.0) / totalBalls;
            response.setCurrentRunRate(round(runRate));
        }

        if (innings.getTarget() != null) {

            int runsReq = innings.getTarget() - innings.getTotalRuns();
            response.setRunsRequired(Math.max(runsReq, 0));

            int ballsRemaining =
                    (innings.getMaxOvers() * 6) - innings.getTotalValidBalls();

            if (ballsRemaining > 0) {
                double rrr = (runsReq * 6.0) / ballsRemaining;
                response.setRequiredRunRate(round(rrr));
            }
        }

        response.setStriker(buildPlayerScore(
                innings.getStriker().getId(),
                match,
                innings
        ));

        response.setNonStriker(buildPlayerScore(
                innings.getNonStriker().getId(),
                match,
                innings
        ));

        response.setCurrentBowler(
                buildBowlerScore(
                        innings.getCurrentBowler().getId(),
                        match,
                        innings
                )
        );

        response.setCurrentOverBalls(
                ballEventRepository
                        .findTop6ByInningsAndIsUndoneFalseOrderByIdDesc(innings)
                        .stream()
                        .map(this::formatBall)
                        .toList()
        );

        return response;
    }

    private PlayerScore buildPlayerScore(
            Long playerId,
            Match match,
            MatchInnings innings
    ) {

        PlayerScore score = new PlayerScore();

        score.setUserId(playerId);
        score.setName(userRepository.findById(playerId)
                .map(User::getEmail)
                .orElse("Player"));

        int runs = ballEventRepository.sumRunsByBatsman(playerId, innings.getId());
        int balls = ballEventRepository.countBallsByBatsman(playerId, innings.getId());

        score.setRuns(runs);
        score.setBalls(balls);

        if (balls > 0) {
            score.setStrikeRate(round((runs * 100.0) / balls));
        }

        return score;
    }

    private BowlerScore buildBowlerScore(
            Long bowlerId,
            Match match,
            MatchInnings innings
    ) {

        BowlerScore score = new BowlerScore();

        score.setUserId(bowlerId);
        score.setName(userRepository.findById(bowlerId)
                .map(User::getEmail)
                .orElse("Bowler"));

        int runs = ballEventRepository.sumRunsByBowler(bowlerId, innings.getId());
        int balls = ballEventRepository.countBallsByBowler(bowlerId, innings.getId());
        int wickets = ballEventRepository.countWicketsByBowler(bowlerId, innings.getId());

        score.setRunsConceded(runs);
        score.setWickets(wickets);

        score.setOvers(balls / 6);
        score.setBalls(balls % 6);

        if (balls > 0) {
            double economy = (runs * 6.0) / balls;
            score.setEconomy(round(economy));
        }

        return score;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String formatBall(BallEvent event) {

        if (event.getBallResultType() == BallResultType.WIDE) {
            return "Wd";
        }

        if (event.getBallResultType() == BallResultType.NO_BALL) {
            return "Nb";
        }

        if (event.isWicket()) {
            return "W";
        }

        if (event.getExtraType() != null) {
            switch (event.getExtraType()) {
                case BYE:
                    return "B" + event.getExtraRuns();
                case LEG_BYE:
                    return "Lb" + event.getExtraRuns();
                default:
                    break;
            }
        }

        return String.valueOf(event.getRunsOffBat());
    }

    @Transactional
    public void undoLastBall(Long matchId, Long scorerUserId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!match.getScorerUserId().equals(scorerUserId)) {
            throw new IllegalStateException("Only scorer allowed");
        }

        MatchInnings innings = inningsRepository.findByMatch(match)
                .orElseThrow(() -> new RuntimeException("Innings not found"));

        BallEvent lastBall = ballEventRepository
                .findTopByInningsAndIsUndoneFalseOrderByIdDesc(innings);

        if (lastBall == null) {
            throw new IllegalStateException("No balls to undo");
        }


        lastBall.setUndone(true);
        ballEventRepository.save(lastBall);
        reverseBallImpact(lastBall, innings, match);
        validateOverIntegrity(innings);


        inningsRepository.save(innings);
        matchRepository.save(match);
    }
    private void reverseBallImpact(
            BallEvent event,
            MatchInnings innings,
            Match match) {

        // ---------------- RUN RESTORE ----------------
        int runsThisBall;

        if (event.isWicket()
                && event.getWicketType() == WicketType.RUN_OUT
                && event.getRunsCompletedBeforeWicket() != null) {

            runsThisBall =
                    event.getRunsCompletedBeforeWicket()
                            + event.getExtraRuns();
        } else {
            runsThisBall =
                    event.getRunsOffBat()
                            + event.getExtraRuns();
        }

        innings.setTotalRuns(
                innings.getTotalRuns() - runsThisBall
        );


        // ---------------- DELIVERY RESTORE ----------------
        if (event.getBallResultType() == BallResultType.NORMAL) {

            boolean wasSixthBall =
                    event.getBallNumber() == 5;

            // Restore over & ball
            if (wasSixthBall) {
                innings.setCurrentOver(
                        innings.getCurrentOver() - 1
                );
                innings.setCurrentBall(5);

                // Reverse over-end strike swap
                swapStrike(innings);
            } else {
                innings.setCurrentBall(
                        innings.getCurrentBall() - 1
                );
            }

            innings.setTotalValidBalls(
                    innings.getTotalValidBalls() - 1
            );

            // Reverse strike for runs
            int runsForStrike =
                    event.isWicket()
                            && event.getWicketType() == WicketType.RUN_OUT
                            && event.getRunsCompletedBeforeWicket() != null
                            ? event.getRunsCompletedBeforeWicket()
                            : event.getRunsOffBat();

            if (runsForStrike % 2 == 1) {
                swapStrike(innings);
            }
        }


        // ---------------- WICKET RESTORE ----------------
        if (event.isWicket()) {

            innings.setTotalWickets(
                    innings.getTotalWickets() - 1
            );

            fallOfWicketRepository
                    .findByInningsOrderByWicketNumberAsc(innings)
                    .stream()
                    .reduce((first, second) -> second)
                    .ifPresent(fallOfWicketRepository::delete);
        }


        // ---------------- FREE HIT RESTORE ----------------
        if (event.getBallResultType() == BallResultType.NO_BALL) {

            // Undoing a no-ball removes free hit
            innings.setFreeHit(false);

        } else {

            // Fetch previous valid ball (exclude this event)
            BallEvent previous = ballEventRepository
                    .findTopByInningsAndIsUndoneFalseOrderByIdDesc(innings);

            if (previous != null
                    && previous.getBallResultType() == BallResultType.NO_BALL) {

                innings.setFreeHit(true);
            } else {
                innings.setFreeHit(false);
            }
        }


        // ---------------- STATUS RESTORE ----------------
        innings.setStatus(InningsStatus.LIVE);
        innings.setAwaitingBowlerAfterBatsman(false);

        // Restore match state only if target no longer reached
        if (innings.getTarget() != null) {
            if (innings.getTotalRuns() < innings.getTarget()) {
                match.setStatus(MatchStatus.LIVE);
                innings.setStatus(InningsStatus.LIVE);
            }
        }
    }

    private EndOverResponse buildEndOverResponse(
            Match match,
            MatchInnings innings
    ) {
        EndOverResponse response = new EndOverResponse();

        int completedOver = innings.getCurrentOver();

        response.setOverNumber(completedOver);

        // Fetch last 6 valid balls of previous over
        List<BallEvent> balls =
                ballEventRepository
                        .findLastOverBalls(innings, completedOver - 1);

        response.setBallsInOver(
                balls.stream()
                        .map(this::formatBall)
                        .toList()
        );

        int runsInOver = balls.stream()
                .mapToInt(b -> b.getRunsOffBat() + b.getExtraRuns())
                .sum();

        int wicketsInOver = (int) balls.stream()
                .filter(BallEvent::isWicket)
                .count();

        int extrasInOver = balls.stream()
                .mapToInt(BallEvent::getExtraRuns)
                .sum();

        response.setExtrasInOver(extrasInOver);

        response.setRunsInOver(runsInOver);
        response.setWicketsInOver(wicketsInOver);

        response.setTotalRuns(innings.getTotalRuns());
        response.setTotalWickets(innings.getTotalWickets());

        response.setCurrentRunRate(
                calculateRunRate(innings)
        );

        if (innings.getTarget() != null) {
            response.setTarget(innings.getTarget());
            response.setRequiredRunRate(
                    calculateRequiredRunRate(innings)
            );
            response.setChaseEquation(
                    buildChaseEquation(innings)
            );
        }

        response.setBowlerSummary(
                buildBowlerScore(
                        innings.getCurrentBowler().getId(),
                        match,
                        innings
                )
        );

        response.setStriker(
                buildPlayerScore(
                        innings.getStriker().getId(),
                        match,
                        innings
                )
        );

        response.setNonStriker(
                buildPlayerScore(
                        innings.getNonStriker().getId(),
                        match,
                        innings
                )
        );

        return response;
    }

    @Transactional
    public void redoLastBall(Long matchId, Long scorerUserId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!match.getScorerUserId().equals(scorerUserId)) {
            throw new IllegalStateException("Only scorer allowed");
        }

        MatchInnings innings = inningsRepository.findByMatch(match)
                .orElseThrow(() -> new RuntimeException("Innings not found"));

        BallEvent lastUndone = ballEventRepository
                .findTopByInningsAndIsUndoneTrueOrderByIdDesc(innings);

        if (lastUndone == null) {
            throw new IllegalStateException("No balls to redo");
        }

        applyScoringLogic(lastUndone, innings, match);
        validateOverIntegrity(innings);

        lastUndone.setUndone(false);
        ballEventRepository.save(lastUndone);

        inningsRepository.save(innings);
        matchRepository.save(match);
    }

    private void createFallOfWicket(
            BallEvent event,
            MatchInnings innings
    ) {
        FallOfWicket fow = new FallOfWicket();

        fow.setInnings(innings);
        fow.setWicketNumber(innings.getTotalWickets());
        fow.setTeamScoreAtFall(innings.getTotalRuns());
        fow.setOverNumber(innings.getCurrentOver());
        fow.setBallNumber(innings.getCurrentBall());
        fow.setDismissedPlayer(event.getDismissedPlayer());
        fow.setBowler(event.getBowler());
        fow.setWicketType(event.getWicketType());

        fallOfWicketRepository.save(fow);
    }

    private double calculateRunRate(MatchInnings innings) {
        if (innings.getTotalValidBalls() == 0) return 0;
        return round(
                (innings.getTotalRuns() * 6.0)
                        / innings.getTotalValidBalls()
        );
    }

    private double calculateRequiredRunRate(MatchInnings innings) {
        int ballsRemaining =
                (innings.getMaxOvers() * 6)
                        - innings.getTotalValidBalls();

        if (ballsRemaining <= 0) return 0;

        int runsReq =
                innings.getTarget() - innings.getTotalRuns();

        return round((runsReq * 6.0) / ballsRemaining);
    }

    private String buildChaseEquation(MatchInnings innings) {
        int ballsRemaining =
                (innings.getMaxOvers() * 6)
                        - innings.getTotalValidBalls();

        int runsReq =
                innings.getTarget() - innings.getTotalRuns();

        return "Need "
                + runsReq
                + " runs from "
                + ballsRemaining
                + " balls";
    }

    private void validateOverIntegrity(MatchInnings innings) {

        // ❌ Ball cannot exceed 6
        if (innings.getCurrentBall() < 0 || innings.getCurrentBall() > 6) {
            throw new IllegalStateException("Invalid ball count in over");
        }

        // ❌ Over cannot be negative
        if (innings.getCurrentOver() < 0) {
            throw new IllegalStateException("Invalid over count");
        }

        int expectedTotalValidBalls =
                (innings.getCurrentOver() * 6)
                        + innings.getCurrentBall();

        if (innings.getTotalValidBalls() != expectedTotalValidBalls) {
            throw new IllegalStateException(
                    "Over integrity violated: totalValidBalls mismatch"
            );
        }
    }

}
