package in.crewplay.crewplay_backend.domain.match.service;

import in.crewplay.crewplay_backend.domain.match.MatchSquadMember;
import in.crewplay.crewplay_backend.domain.match.repository.MatchSquadMemberRepository;
import in.crewplay.crewplay_backend.domain.match.dto.AssignSquadRolesRequest;
import in.crewplay.crewplay_backend.domain.match.dto.SquadRoleDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchRoleService {

    private final MatchSquadMemberRepository squadMemberRepository;

    @Transactional
    public void assignRoles(Long matchId, AssignSquadRolesRequest request) {

        List<MatchSquadMember> squadMembers =
                squadMemberRepository.findByMatchTeam_Match_IdAndMatchTeam_Team_Id(
                        matchId,
                        request.getTeamId()
                );

        if (squadMembers.isEmpty()) {
            throw new IllegalStateException("No squad found for this team in the match");
        }

        Map<Long, MatchSquadMember> squadMap =
                squadMembers.stream()
                        .collect(Collectors.toMap(
                                m -> m.getUser().getId(),
                                Function.identity()
                        ));

        long captainCount =
                request.getRoles().stream()
                        .filter(SquadRoleDTO::isCaptain)
                        .count();

        if (captainCount != 1) {
            throw new IllegalStateException("Each team must have exactly one captain");
        }

        // Reset roles first (idempotent)
        squadMembers.forEach(m -> {
            m.setCaptain(false);
            m.setWicketKeeper(false);
        });

        // Apply new roles
        for (SquadRoleDTO dto : request.getRoles()) {

            MatchSquadMember member = squadMap.get(dto.getUserId());

            if (member == null) {
                throw new IllegalStateException(
                        "User " + dto.getUserId() + " is not part of this squad"
                );
            }

            member.setCaptain(dto.isCaptain());
            member.setWicketKeeper(dto.isWicketKeeper());
        }

        squadMemberRepository.saveAll(squadMembers);
    }
}
