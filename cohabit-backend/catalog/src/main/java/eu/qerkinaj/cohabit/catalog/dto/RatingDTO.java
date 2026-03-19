package eu.qerkinaj.cohabit.catalog.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RatingDTO(
        UUID id,
        UUID userId,
        UUID targetId,
        int score,
        String comment,
        int helpfulVotes,
        Map<String, Integer> thematicRatings,
        Instant createdAt
) {
}
