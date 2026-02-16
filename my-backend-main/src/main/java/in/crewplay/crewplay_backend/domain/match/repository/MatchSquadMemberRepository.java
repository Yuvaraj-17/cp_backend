package in.crewplay.crewplay_backend.domain.match.repository;

import in.crewplay.crewplay_backend.domain.match.MatchSquadMember;
import in.crewplay.crewplay_backend.domain.match.MatchTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchSquadMemberRepository extends JpaRepository<MatchSquadMember, Long> {
    List<MatchSquadMember> findByMatchTeam(MatchTeam matchTeam);

    List<MatchSquadMember>
    findByMatchTeam_Match_IdAndMatchTeam_Team_Id(Long matchId, Long teamId);
    boolean existsByMatchTeam_Match_IdAndMatchTeam_Team_IdAndUser_Id(
            Long matchId,
            Long teamId,
            Long userId
    );


    boolean existsByMatchTeamAndUser_Id(MatchTeam matchTeam, Long userId);
}
