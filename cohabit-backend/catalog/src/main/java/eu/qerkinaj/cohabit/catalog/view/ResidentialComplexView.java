package eu.qerkinaj.cohabit.catalog.view;

import eu.qerkinaj.cohabit.catalog.dto.ApartmentDTO;
import eu.qerkinaj.cohabit.catalog.dto.ResidentialComplexDTO;
import eu.qerkinaj.cohabit.catalog.dto.RatingDTO;

import java.util.List;

public record ResidentialComplexView(
        ResidentialComplexDTO residentialComplex,
        List<ApartmentDTO> apartments,
        List<RatingDTO> ratings
) {
}
