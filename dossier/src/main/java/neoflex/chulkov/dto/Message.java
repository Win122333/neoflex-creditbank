package neoflex.chulkov.dto;

import neoflex.chulkov.dto.enums.Theme;

public record Message(
        String email,
        Theme theme
) {
}
