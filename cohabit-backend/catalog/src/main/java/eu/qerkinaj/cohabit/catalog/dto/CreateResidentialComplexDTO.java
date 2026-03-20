package eu.qerkinaj.cohabit.catalog.dto;

import java.util.List;

public record CreateResidentialComplexDTO(
        String name,
        String street,
        String houseNumber,
        String zipCode,
        String city,
        String district,
        Double latitude,
        Double longitude,
        List<String> imageUrls
) {
}
