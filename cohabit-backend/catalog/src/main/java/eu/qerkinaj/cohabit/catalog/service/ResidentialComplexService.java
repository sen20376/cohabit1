package eu.qerkinaj.cohabit.catalog.service;

import eu.qerkinaj.cohabit.catalog.client.RatingClient;
import eu.qerkinaj.cohabit.catalog.domain.GeoRegion;
import eu.qerkinaj.cohabit.catalog.domain.Image;
import eu.qerkinaj.cohabit.catalog.domain.ResidentialComplex;
import eu.qerkinaj.cohabit.catalog.dto.ApartmentDTO;
import eu.qerkinaj.cohabit.catalog.dto.CreateResidentialComplexDTO;
import eu.qerkinaj.cohabit.catalog.dto.ResidentialComplexDTO;
import eu.qerkinaj.cohabit.catalog.mapper.CatalogMapper;
import eu.qerkinaj.cohabit.catalog.dto.RatingDTO;
import eu.qerkinaj.cohabit.catalog.view.ResidentialComplexView;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.*;

@ApplicationScoped
public class ResidentialComplexService {

    private static final Logger LOG = Logger.getLogger(ResidentialComplexService.class);

    @Inject
    CatalogMapper mapper;

    @Inject
    @RestClient
    RatingClient ratingClient;

    @Transactional
    public List<ResidentialComplexDTO> getComplexesByOwner(UUID ownerId) {
        return mapper.toComplexDTOs(ResidentialComplex.list("ownerId", ownerId));
    }

    @Transactional
    public List<ResidentialComplexDTO> getAllComplexes() {
        LOG.info("Fetching all residential complexes from database...");

        List<ResidentialComplex> entities = ResidentialComplex.listAll();

        LOG.infof("Found %d complexes.", entities.size());
        return mapper.toComplexDTOs(entities);
    }

    @Transactional
    public ResidentialComplexView getComplexDetails(UUID id) {
        LOG.infof("Fetching details for complex ID: %s", id);

        ResidentialComplex complex = ResidentialComplex.findById(id);

        if (complex == null) {
            LOG.warnf("Complex with ID %s not found in database.", id);
            throw new NotFoundException("Complex not found: " + id);
        }

        ResidentialComplexDTO complexDTO = mapper.toDTO(complex);
        List<ApartmentDTO> apartmentDTOs = mapper.toApartmentDTOs(
                complex.apartments.stream().filter(a -> a.active).toList()
        );

        LOG.debugf("Found %d apartments for complex %s", apartmentDTOs.size(), complex.name);

        List<RatingDTO> ratings;
        try {
            LOG.debug("Calling Rating-Service...");
            ratings = ratingClient.getRatings(id);
            LOG.infof("Received %d ratings for complex %s", ratings.size(), id);
        } catch (Exception e) {
            LOG.errorf(e, "Could not fetch ratings for Complex %s. Service might be down.", id);
            ratings = Collections.emptyList();
        }

        return new ResidentialComplexView(complexDTO, apartmentDTOs, ratings);
    }

    @Transactional
    public void createComplex(CreateResidentialComplexDTO dto, UUID ownerId) {
        LOG.infof("Creating complex '%s' for Owner [%s]", dto.name(), ownerId);

        ResidentialComplex complex = new ResidentialComplex();
        complex.ownerId = ownerId;

        complex.name = dto.name();
        complex.street = dto.street();
        complex.houseNumber = dto.houseNumber();
        complex.zipCode = dto.zipCode();
        complex.city = dto.city();

        if (dto.latitude() != null && dto.longitude() != null) {
            complex.location = createPoint(dto.latitude(), dto.longitude());
        }

        if (dto.district() != null) {
            GeoRegion region = GeoRegion.find("name", dto.district()).firstResult();
            if (region != null) complex.geoRegion = region;
        }

        complex.persist();

        if (dto.imageUrls() != null) {
            for (int i = 0; i < dto.imageUrls().size(); i++) {
                String url = dto.imageUrls().get(i);
                if (url != null && !url.isBlank()) {
                    Image img = new Image();
                    img.url = url;
                    img.complex = complex;
                    img.isPrimary = (i == 0);
                    img.persist();
                }
            }
        }

        LOG.infof("Persisted complex %s with ID %s", complex.name, complex.id);
    }

    @Transactional
    public void updateComplex(UUID id, CreateResidentialComplexDTO dto, UUID userId) {
        ResidentialComplex complex = ResidentialComplex.findById(id);
        if (complex == null) {
            throw new NotFoundException("Anlage nicht gefunden");
        }

        if (complex.ownerId == null || !complex.ownerId.equals(userId)) {
            LOG.warnf("User %s tried to edit complex %s owned by %s", userId, id, complex.ownerId);
            throw new ForbiddenException("Du darfst nur deine eigenen Anlagen bearbeiten.");
        }

        complex.name = dto.name();
        complex.street = dto.street();
        complex.houseNumber = dto.houseNumber();
        complex.zipCode = dto.zipCode();
        complex.city = dto.city();

        if (dto.latitude() != null && dto.longitude() != null) {
            complex.location = createPoint(dto.latitude(), dto.longitude());
        }

        if (dto.district() != null) {
            GeoRegion region = GeoRegion.find("name", dto.district()).firstResult();
            complex.geoRegion = region;
        }

        LOG.infof("Complex %s updated by owner %s", id, userId);
    }

    @Transactional
    public void deleteComplex(UUID id, UUID userId) {
        ResidentialComplex complex = ResidentialComplex.findById(id);
        if (complex == null) {
            throw new NotFoundException("Anlage nicht gefunden");
        }

        if (complex.ownerId == null || !complex.ownerId.equals(userId)) {
            throw new ForbiddenException("Du darfst nur deine eigenen Anlagen löschen.");
        }

        complex.delete();

        LOG.infof("Complex %s deleted by owner %s", id, userId);
    }

    private Point createPoint(Double latitude, Double longitude) {
        GeometryFactory gf = new GeometryFactory();
        Point point = gf.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }

    @Transactional
    public List<ResidentialComplexDTO> searchComplexes(String searchTerm, String district) {
        LOG.infof("Searching complexes. Filter: Term='%s', District='%s'", searchTerm, district);

        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (searchTerm != null && !searchTerm.isBlank()) {
            query.append(" AND (lower(name) LIKE :search OR lower(street) LIKE :search)");
            params.put("search", "%" + searchTerm.toLowerCase() + "%");
        }

        if (district != null && !district.isBlank()) {
            query.append(" AND lower(zipCode) LIKE :district");
            params.put("district", "%" + district.toLowerCase() + "%");
        }

        List<ResidentialComplex> entities = ResidentialComplex.list(query.toString(), params);

        LOG.infof("Search returned %d results.", entities.size());

        return mapper.toComplexDTOs(entities);
    }
}