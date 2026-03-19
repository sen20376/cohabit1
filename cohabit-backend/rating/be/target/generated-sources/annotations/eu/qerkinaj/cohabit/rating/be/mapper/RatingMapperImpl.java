package eu.qerkinaj.cohabit.rating.be.mapper;

import eu.qerkinaj.cohabit.rating.api.dto.RatingDTO;
import eu.qerkinaj.cohabit.rating.api.dto.RatingInput;
import eu.qerkinaj.cohabit.rating.be.domain.Rating;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-19T22:58:11+0100",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@ApplicationScoped
public class RatingMapperImpl implements RatingMapper {

    @Override
    public Rating toEntity(RatingInput input, UUID userId) {
        if ( input == null && userId == null ) {
            return null;
        }

        Rating rating = new Rating();

        if ( input != null ) {
            rating.comment = input.comment();
            rating.score = input.score();
            rating.targetId = input.targetId();
            Map<String, Integer> map = input.thematicRatings();
            if ( map != null ) {
                rating.thematicRatings = new LinkedHashMap<String, Integer>( map );
            }
        }
        rating.userId = userId;

        return rating;
    }

    @Override
    public RatingDTO toResponse(Rating entity) {
        if ( entity == null ) {
            return null;
        }

        UUID id = null;
        UUID userId = null;
        UUID targetId = null;
        int score = 0;
        String comment = null;
        int helpfulVotes = 0;
        Map<String, Integer> thematicRatings = null;
        Instant createdAt = null;

        id = entity.id;
        userId = entity.userId;
        targetId = entity.targetId;
        score = entity.score;
        comment = entity.comment;
        helpfulVotes = entity.helpfulVotes;
        Map<String, Integer> map = entity.thematicRatings;
        if ( map != null ) {
            thematicRatings = new LinkedHashMap<String, Integer>( map );
        }
        createdAt = entity.createdAt;

        RatingDTO ratingDTO = new RatingDTO( id, userId, targetId, score, comment, helpfulVotes, thematicRatings, createdAt );

        return ratingDTO;
    }
}
