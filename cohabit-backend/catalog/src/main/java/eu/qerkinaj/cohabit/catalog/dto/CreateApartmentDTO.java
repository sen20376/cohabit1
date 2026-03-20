package eu.qerkinaj.cohabit.catalog.dto;

import java.util.List;
import java.util.UUID;

public record CreateApartmentDTO(
        UUID complexId,
        String title,
        String description,
        String doorNumber,
        Integer floor,
        Double sizeSqm,
        List<String> imageUrls
) {
}
