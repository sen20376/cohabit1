package eu.qerkinaj.cohabit.catalog.resource;

import eu.qerkinaj.cohabit.catalog.dto.ComplexSearchDTO;
import eu.qerkinaj.cohabit.catalog.dto.CreateResidentialComplexDTO;
import eu.qerkinaj.cohabit.catalog.dto.ResidentialComplexDTO;
import eu.qerkinaj.cohabit.catalog.service.ResidentialComplexService;
import eu.qerkinaj.cohabit.catalog.view.ResidentialComplexView;
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
public class ResidentialComplexResource {

    private static final Logger LOG = Logger.getLogger(ResidentialComplexResource.class);

    @Inject
    ResidentialComplexService residentialComplexService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/complexes")
    @PermitAll
    public List<ResidentialComplexDTO> getAll() {
        String userId = getUserIdSafe();
        LOG.infof("User [%s] is requesting all residential complexes", userId);

        return residentialComplexService.getAllComplexes();
    }

    @GET
    @Path("/complexes/{id}")
    @PermitAll
    public ResidentialComplexView getComplexDetails(@PathParam("id") UUID id) {
        LOG.infof("User [%s] requests details for complex [%s]", getUserIdSafe(), id);

        return residentialComplexService.getComplexDetails(id);
    }

    @POST
    @Path("/complexes")
    @RolesAllowed({Role.VENDOR})
    public void createComplex(CreateResidentialComplexDTO dto) {
        String vendorIdRaw = jwt.getSubject();

        UUID vendorId = UUID.fromString(vendorIdRaw);

        LOG.infof("Vendor [%s] creating complex '%s'", vendorId, dto.name());

        residentialComplexService.createComplex(dto, vendorId);
    }

    @PUT
    @Path("/complexes/{id}")
    @RolesAllowed({Role.VENDOR})
    public void updateComplex(@PathParam("id") UUID id, CreateResidentialComplexDTO dto) {
        String vendorIdRaw = jwt.getSubject();
        UUID vendorId = UUID.fromString(vendorIdRaw);

        LOG.infof("Vendor [%s] updating complex [%s]", vendorId, id);

        residentialComplexService.updateComplex(id, dto, vendorId);
    }

    @DELETE
    @Path("/complexes/{id}")
    @RolesAllowed({Role.VENDOR})
    public void deleteComplex(@PathParam("id") UUID id) {
        String vendorIdRaw = jwt.getSubject();
        UUID vendorId = UUID.fromString(vendorIdRaw);

        LOG.infof("Vendor [%s] deleting complex [%s]", vendorId, id);

        residentialComplexService.deleteComplex(id, vendorId);
    }


    @GET
    @Path("/complexes/search")
    @PermitAll
    public List<ResidentialComplexDTO> searchComplexes(@QueryParam("q") String searchTerm,
                                                       @QueryParam("district") String district) {
        String userId = getUserIdSafe();
        LOG.infof("User [%s] searching complexes - Query: '%s', District: '%s'",
                userId, searchTerm, district);

        var filter = new ComplexSearchDTO(searchTerm, district);
        return residentialComplexService.searchComplexes(filter);
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