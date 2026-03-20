package eu.qerkinaj.cohabit.catalog.dto;

import java.util.List;
import java.util.UUID;

public record ApartmentDTO(
        UUID id,
        UUID complexId,
        UUID ownerId,
        String title,
        String doorNumber,
        Integer floor,
        Double sizeSqm,
        String description,
        Double avgRating,
        Long viewCount,
        boolean active,
        String city,
        List<String> imageUrls
) {
}
