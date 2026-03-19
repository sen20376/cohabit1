package eu.qerkinaj.cohabit.catalog.resource;

import eu.qerkinaj.cohabit.catalog.dto.ApartmentDTO;
import eu.qerkinaj.cohabit.catalog.dto.ApartmentFilterDTO;
import eu.qerkinaj.cohabit.catalog.dto.CreateApartmentDTO;
import eu.qerkinaj.cohabit.catalog.service.ApartmentService;
import eu.qerkinaj.cohabit.catalog.view.ApartmentView;
import eu.qerkinaj.cohabit.common.Role;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/catalog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApartmentResource {

    private static final Logger LOG = Logger.getLogger(ApartmentResource.class);

    @Inject
    ApartmentService apartmentService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/apartments/{id}")
    @PermitAll
    public ApartmentView getApartmentDetails(@PathParam("id") UUID id) {
        String userId = getUserIdSafe();
        LOG.infof("User [%s] requests details for apartment [%s]", userId, id);

        return apartmentService.getApartmentDetails(id);
    }

    @GET
    @Path("/apartments")
    @PermitAll
    public List<ApartmentDTO> getAll() {
        String userId = getUserIdSafe();
        LOG.infof("User [%s] requests list of all apartments", userId);

        return apartmentService.getAllApartments();
    }

    @POST
    @Path("/apartments")
    @RolesAllowed(Role.VENDOR)
    public void createApartment(CreateApartmentDTO dto) {
        String userIdStr = jwt.getSubject();

        LOG.infof("Vendor [%s] attempts to create apartment: Title='%s', ComplexId='%s'",
                userIdStr, dto.title(), dto.complexId());

        if (userIdStr == null) {
            LOG.warn("No user ID found in token for creation. Generating random UUID (Test-Mode?).");
            userIdStr = UUID.randomUUID().toString();
        }

        UUID ownerId = UUID.fromString(userIdStr);
        apartmentService.createApartment(dto, ownerId);

        LOG.infof("Apartment created successfully by [%s]", userIdStr);
    }

    @GET
    @Path("/apartments/search")
    @PermitAll
    public List<ApartmentDTO> searchApartments(@QueryParam("minSize") Double minSize,
                                               @QueryParam("maxSize") Double maxSize,
                                               @QueryParam("district") String district,
                                               @QueryParam("complex") String complexName,
                                               @QueryParam("address") String address) {

        String userId = getUserIdSafe();
        LOG.infof("User [%s] searching apartments with filters - Size: %s-%s, District: %s, Complex: %s",
                userId, minSize, maxSize, district, complexName);

        var filter = new ApartmentFilterDTO(minSize, maxSize, null, district, complexName, address);
        return apartmentService.searchApartments(filter);
    }

    @PUT
    @Path("/apartments/{id}")
    @RolesAllowed(Role.VENDOR)
    public void updateApartment(@PathParam("id") UUID id, CreateApartmentDTO dto) {
        String userIdStr = jwt.getSubject();
        apartmentService.updateApartment(id, dto, UUID.fromString(userIdStr));
    }

    @PATCH
    @Path("/apartments/{id}/active")
    @RolesAllowed(Role.VENDOR)
    public void toggleActive(@PathParam("id") UUID id) {
        String userIdStr = jwt.getSubject();
        apartmentService.toggleActive(id, UUID.fromString(userIdStr));
    }

    @DELETE
    @Path("/apartments/{id}")
    @RolesAllowed(Role.VENDOR)
    public void deleteApartment(@PathParam("id") UUID id) {
        String userIdStr = jwt.getSubject();
        apartmentService.deleteApartment(id, UUID.fromString(userIdStr));
    }

    @PUT
    @Path("/apartments/{id}/rating")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateRating(@PathParam("id") UUID id, Double newAverage) {
        LOG.infof("Received rating update for apartment [%s]: %s", id, newAverage);
        apartmentService.updateRating(id, newAverage);
    }

    private String getUserIdSafe() {
        try {
            String subject = jwt.getSubject();
            return subject != null ? subject : "Anonymous";
        } catch (Exception e) {
            return "Anonymous";
        }
    }
}