package eu.qerkinaj.cohabit.catalog.service;

import eu.qerkinaj.cohabit.catalog.client.RatingClient;
import eu.qerkinaj.cohabit.catalog.domain.Apartment;
import eu.qerkinaj.cohabit.catalog.domain.ResidentialComplex;
import eu.qerkinaj.cohabit.catalog.dto.ApartmentDTO;
import eu.qerkinaj.cohabit.catalog.dto.ApartmentFilterDTO;
import eu.qerkinaj.cohabit.catalog.dto.CreateApartmentDTO;
import eu.qerkinaj.cohabit.catalog.mapper.CatalogMapper;
import eu.qerkinaj.cohabit.catalog.view.ApartmentView;
import eu.qerkinaj.cohabit.rating.api.dto.RatingDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.*;

@ApplicationScoped
public class ApartmentService {

    private static final Logger LOG = Logger.getLogger(ApartmentService.class);

    @Inject
    CatalogMapper mapper;

    @Inject
    @RestClient
    RatingClient ratingClient;

    public List<ApartmentDTO> getAllApartments() {
        LOG.info("Fetching all apartments from database...");

        List<Apartment> entities = Apartment.listAll();

        LOG.infof("Found %d apartments.", entities.size());
        return mapper.toApartmentDTOs(entities);
    }

    @Transactional
    public ApartmentView getApartmentDetails(UUID id) {
        LOG.infof("Fetching details for apartment ID: %s", id);

        Apartment apartment = Apartment.findById(id);
        if (apartment == null) {
            LOG.warnf("Apartment with ID %s not found.", id);
            throw new NotFoundException("Apartment not found: " + id);
        }

        if (apartment.viewCount == null) apartment.viewCount = 0L;
        apartment.viewCount++;

        ApartmentDTO apartmentDTO = mapper.toDTO(apartment);

        List<RatingDTO> ratings;
        try {
            LOG.debugf("Calling Rating-Service for target %s...", id);
            ratings = ratingClient.getRatings(id);
            LOG.infof("Received %d ratings for apartment %s", ratings.size(), id);
        } catch (Exception e) {
            LOG.errorf(e, "Could not fetch ratings for Apartment %s. Service might be unavailable.", id);
            ratings = Collections.emptyList();
        }

        return new ApartmentView(apartmentDTO, ratings);
    }

    @Transactional
    public void createApartment(CreateApartmentDTO dto, UUID ownerId) {
        LOG.infof("Creating new apartment: Title='%s', ComplexID='%s', Owner='%s'",
                dto.title(), dto.complexId(), ownerId);

        ResidentialComplex complex = ResidentialComplex.findById(dto.complexId());
        if (complex == null) {
            LOG.warnf("Creation failed: ResidentialComplex %s does not exist.", dto.complexId());
            throw new NotFoundException("Complex not found");
        }

        Apartment apt = new Apartment();
        apt.complex = complex;
        apt.ownerId = ownerId;
        apt.title = dto.title();
        apt.description = dto.description();
        apt.doorNumber = dto.doorNumber();
        apt.floor = dto.floor();
        apt.sizeSqm = dto.sizeSqm();
        apt.active = true;

        apt.persist();

        LOG.infof("Apartment created successfully. Assigned ID: %s", apt.id);
    }

    public List<ApartmentDTO> searchApartments(ApartmentFilterDTO filter) {
        LOG.infof("Searching apartments. Filters - MinSize: %s, MaxSize: %s, District: '%s', Complex: '%s', Address: '%s'",
                filter.minSize(), filter.maxSize(), filter.district(), filter.complexName(), filter.address());

        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (filter.minSize() != null) {
            queryBuilder.append(" AND sizeSqm >= :minSize");
            params.put("minSize", filter.minSize());
        }

        if (filter.maxSize() != null) {
            queryBuilder.append(" AND sizeSqm <= :maxSize");
            params.put("maxSize", filter.maxSize());
        }

        if (filter.district() != null && !filter.district().isBlank()) {
            String searchDist = "%" + filter.district().toLowerCase() + "%";
            queryBuilder.append(" AND lower(complex.geoRegion.name) LIKE :dist");
            params.put("dist", searchDist);
        }

        if (filter.complexName() != null && !filter.complexName().isBlank()) {
            queryBuilder.append(" AND lower(complex.name) LIKE :cName");
            params.put("cName", "%" + filter.complexName().toLowerCase() + "%");
        }

        if (filter.address() != null && !filter.address().isBlank()) {
            String searchAddr = "%" + filter.address().toLowerCase() + "%";

            queryBuilder.append(" AND (lower(complex.street) LIKE :addr " +
                    "OR lower(complex.city) LIKE :addr " +
                    "OR lower(complex.zipCode) LIKE :addr)");

            params.put("addr", searchAddr);
        }

        queryBuilder.append(" AND active = true");

        List<Apartment> entities = Apartment.list(queryBuilder.toString(), params);

        LOG.infof("Search finished. Found %d matching apartments.", entities.size());

        return mapper.toApartmentDTOs(entities);
    }

    @Transactional
    public void updateApartment(UUID id, CreateApartmentDTO dto, UUID userId) {
        Apartment apt = Apartment.findById(id);
        if (apt == null) {
            throw new NotFoundException("Wohnung nicht gefunden");
        }

        if (!apt.ownerId.equals(userId)) {
            LOG.warnf("User %s tried to modify apartment %s belonging to %s", userId, id, apt.ownerId);
            throw new ForbiddenException("Du darfst nur deine eigenen Wohnungen bearbeiten.");
        }

        apt.title = dto.title();
        apt.description = dto.description();
        apt.sizeSqm = dto.sizeSqm();
        apt.doorNumber = dto.doorNumber();
        apt.floor = dto.floor();

        LOG.infof("Apartment %s updated by owner %s", id, userId);
    }

    @Transactional
    public void updateRating(UUID id, Double newAverage) {
        Apartment apt = Apartment.findById(id);
        if (apt != null) {
            apt.avgRating = newAverage;
            LOG.infof("Updated average rating for apartment %s to %s", id, newAverage);
        } else {
            LOG.warnf("Apartment %s not found during rating update", id);
        }
    }

    @Transactional
    public void toggleActive(UUID id, UUID userId) {
        Apartment apt = Apartment.findById(id);
        if (apt == null) {
            throw new NotFoundException("Wohnung nicht gefunden");
        }
        if (!apt.ownerId.equals(userId)) {
            throw new ForbiddenException("Du darfst nur deine eigenen Wohnungen ändern.");
        }
        apt.active = !apt.active;
        LOG.infof("Apartment %s active status set to %s by owner %s", id, apt.active, userId);
    }

    @Transactional
    public void deleteApartment(UUID id, UUID userId) {
        Apartment apt = Apartment.findById(id);
        if (apt == null) {
            throw new NotFoundException("Wohnung nicht gefunden");
        }

        if (!apt.ownerId.equals(userId)) {
            throw new ForbiddenException("Du darfst nur deine eigenen Wohnungen löschen.");
        }

        apt.delete();

        LOG.infof("Apartment %s deleted by owner %s", id, userId);
    }


}