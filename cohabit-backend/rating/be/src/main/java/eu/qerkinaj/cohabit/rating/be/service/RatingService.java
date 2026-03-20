package eu.qerkinaj.cohabit.rating.be.service;

import eu.qerkinaj.cohabit.rating.be.dto.RatingDTO;
import eu.qerkinaj.cohabit.rating.be.dto.RatingInput;
import eu.qerkinaj.cohabit.rating.be.CatalogClient;
import eu.qerkinaj.cohabit.rating.be.domain.Rating;
import eu.qerkinaj.cohabit.rating.be.domain.RatingCriteriaDefinition;
import eu.qerkinaj.cohabit.rating.be.domain.RatingVote;
import eu.qerkinaj.cohabit.rating.be.mapper.RatingMapper;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RatingService {

    private static final Logger LOG = Logger.getLogger(RatingService.class);

    @Inject
    RatingMapper mapper;

    @Inject
    @RestClient
    CatalogClient catalogClient;

    @Transactional
    public RatingDTO addRating(RatingInput input, UUID currentUserId) {
        LOG.infof("User [%s] is submitting a rating for target [%s]. Score: %d",
                currentUserId, input.targetId(), input.score());

        if (input.thematicRatings() != null && !input.thematicRatings().isEmpty()) {
            validateThematicKeys(input.thematicRatings().keySet());
        }

        Rating rating = mapper.toEntity(input, currentUserId);
        rating.persist();

        LOG.infof("Rating persisted successfully. New Rating-ID: %s", rating.id);

        updateCatalogAverage(rating.targetId);

        return mapper.toResponse(rating);
    }

    public List<RatingDTO> getRatingsForTarget(UUID targetId) {
        LOG.debugf("Fetching all ratings for target [%s]...", targetId);

        List<RatingDTO> results = Rating.<Rating>list("targetId", Sort.descending("createdAt"), targetId)
                .stream()
                .map(mapper::toResponse)
                .toList();

        LOG.infof("Found %d ratings for target [%s]", results.size(), targetId);
        return results;
    }

    private void validateThematicKeys(Set<String> submittedKeys) {
        Set<String> allowedKeys = RatingCriteriaDefinition.<RatingCriteriaDefinition>list("active", true)
                .stream()
                .map(def -> def.key)
                .collect(Collectors.toSet());

        List<String> invalidKeys = submittedKeys.stream()
                .filter(key -> !allowedKeys.contains(key))
                .toList();

        if (!invalidKeys.isEmpty()) {
            LOG.warnf("Validation failed: Unknown rating criteria keys submitted: %s", invalidKeys);
            throw new BadRequestException("Unbekannte Bewertungskriterien: " + invalidKeys);
        }
    }

    private void updateCatalogAverage(UUID targetId) {
        try {
            LOG.debugf("Calculating new average for target [%s]...", targetId);

            Object avgResult = Rating.getEntityManager()
                    .createQuery("SELECT COALESCE(AVG(r.score), 0.0) FROM Rating r WHERE r.targetId = :targetId")
                    .setParameter("targetId", targetId)
                    .getSingleResult();

            double avg = avgResult instanceof Number ? ((Number) avgResult).doubleValue() : 0.0;
            double roundedAvg = Math.round(avg * 10.0) / 10.0;

            LOG.infof("New calculated average for [%s] is %s.", targetId, roundedAvg);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to update average in Catalog Service for target [%s]. Catalog might be out of sync.", targetId);
        }
    }

    @Transactional
    public RatingDTO updateRating(UUID ratingId, RatingInput input, UUID userId) {
        Rating rating = Rating.findById(ratingId);
        if (rating == null) {
            throw new NotFoundException("Bewertung nicht gefunden");
        }

        if (!rating.userId.equals(userId)) {
            throw new ForbiddenException("Du darfst nur deine eigenen Bewertungen bearbeiten.");
        }

        rating.score = input.score();
        rating.comment = input.comment();
        rating.thematicRatings = input.thematicRatings();

        if (input.thematicRatings() != null && !input.thematicRatings().isEmpty()) {
            validateThematicKeys(input.thematicRatings().keySet());
        }

        updateCatalogAverage(rating.targetId);

        LOG.infof("Rating %s updated by user %s", ratingId, userId);
        return mapper.toResponse(rating);
    }

    @Transactional
    public void deleteRating(UUID ratingId, UUID userId) {
        Rating rating = Rating.findById(ratingId);
        if (rating == null) {
            throw new NotFoundException("Bewertung nicht gefunden");
        }

        if (!rating.userId.equals(userId)) {
            throw new ForbiddenException("Du kannst nur deine eigenen Bewertungen löschen.");
        }

        UUID targetId = rating.targetId;
        rating.delete();

        updateCatalogAverage(targetId);

        LOG.infof("Rating %s deleted by user %s", ratingId, userId);
    }

    @Transactional
    public void voteHelpful(UUID ratingId, UUID userId) {
        if (userId != null && RatingVote.hasVoted(ratingId, userId)) {
            throw new BadRequestException("Du hast diese Bewertung bereits als hilfreich markiert.");
        }

        Rating rating = Rating.findById(ratingId);
        if (rating == null) throw new NotFoundException();

        if (userId != null) {
            RatingVote vote = new RatingVote();
            vote.ratingId = ratingId;
            vote.userId = userId;
            vote.persist();
        }

        rating.helpfulVotes++;

        LOG.infof("User %s voted helpful on rating %s", userId != null ? userId : "guest", ratingId);
    }
}