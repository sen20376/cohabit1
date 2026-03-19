package eu.qerkinaj.cohabit.rating.be.dto;

import java.util.Map;
import java.util.UUID;

public record RatingInput(
        UUID targetId,
        int score,
        String comment,
        Map<String, Integer> thematicRatings
) {
}
