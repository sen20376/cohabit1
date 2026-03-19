package eu.qerkinaj.cohabit.catalog.view;

import eu.qerkinaj.cohabit.catalog.dto.ApartmentDTO;
import eu.qerkinaj.cohabit.catalog.dto.RatingDTO;

import java.util.List;

public record ApartmentView(
        ApartmentDTO apartment,
        List<RatingDTO> ratings
) {
}
