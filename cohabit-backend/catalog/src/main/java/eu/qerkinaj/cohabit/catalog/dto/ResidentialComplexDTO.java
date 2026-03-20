package eu.qerkinaj.cohabit.catalog.dto;

import java.util.List;
import java.util.UUID;

public record ResidentialComplexDTO(
        UUID id,
        UUID ownerId,
        String name,
        String address,
        String city,
        String zipCode,
        String street,
        String houseNumber,
        String district,
        Double averageRating,
        List<String> imageUrls
) {
}
