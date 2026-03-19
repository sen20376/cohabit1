package eu.qerkinaj.cohabit.rating.be.mapper;

import eu.qerkinaj.cohabit.rating.api.dto.RatingInput;
import eu.qerkinaj.cohabit.rating.api.dto.RatingDTO;
import eu.qerkinaj.cohabit.rating.be.domain.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "jakarta-cdi")
public interface RatingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "helpfulVotes", ignore = true)
    @Mapping(target = "userId", source = "userId")
    Rating toEntity(RatingInput input, UUID userId);

    RatingDTO toResponse(Rating entity);
}
