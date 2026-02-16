package in.crewplay.crewplay_backend.team.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TeamCodeGenerator {

    public String generate() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
