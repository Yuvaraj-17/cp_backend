package in.crewplay.crewplay_backend.team_roster.repository;

import in.crewplay.crewplay_backend.domain.teams.Team;
import in.crewplay.crewplay_backend.domain.teams.TeamMember;
import in.crewplay.crewplay_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByTeamAndUser(Team team, User user);

    List<TeamMember> findByTeam(Team team);

    List<TeamMember> findByTeam_Id(Long teamId);

    boolean existsByTeam_IdAndUser_Id(Long teamId, Long userId);


    boolean existsByTeamAndUser(Team team, User user);


    boolean existsByTeam_IdAndMobileNumber(Long teamId, String mobileNumber);

    long countByTeam_Id(Long teamId);



}
