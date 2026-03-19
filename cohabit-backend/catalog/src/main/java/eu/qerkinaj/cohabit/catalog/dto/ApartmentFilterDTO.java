package eu.qerkinaj.cohabit.catalog.dto;

public record ApartmentFilterDTO(
        Double minSize,
        Double maxSize,
        String district,
        String complexName,
        String address
) {
}
