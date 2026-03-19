package eu.qerkinaj.cohabit.user.resource;

import eu.qerkinaj.cohabit.user.dto.UserDTO;
import eu.qerkinaj.cohabit.user.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Logger LOG = Logger.getLogger(UserResource.class);

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Authenticated
    @Path("/me")
    public UserDTO me() {
        String userIdStr = null;

        Object userIdClaim = jwt.getClaim("userId");
        if (userIdClaim != null) {
            userIdStr = userIdClaim.toString();
        }

        if (userIdStr == null) {
            userIdStr = jwt.getSubject();
        }

        LOG.infof("User identifying as [%s] is requesting profile data (/me)", userIdStr);

        try {
            UUID uuid = UUID.fromString(userIdStr);
            return userService.getUser(uuid);
        } catch (IllegalArgumentException | NullPointerException e) {
            LOG.errorf("Invalid UUID in Token: %s. Token payload might be wrong.", userIdStr);
            throw new BadRequestException("Token enthält keine gültige User-ID (UUID erwartet).");
        }
    }

    @GET
    @Path("/{userid}")
    @Authenticated
    public String getUserEmail(@PathParam("userid") String id) {
        String requestingUser = jwt.getName();

        LOG.infof("User [%s] is requesting email address for User-ID [%s]", requestingUser, id);

        return userService.getUserEmail(UUID.fromString(id));
    }

    @GET
    @Path("/me/bookmarks")
    @Authenticated
    public List<UUID> getBookmarks() {
        UUID userId = resolveUserId();
        return userService.getBookmarks(userId);
    }

    @POST
    @Path("/me/bookmarks/{apartmentId}")
    @Authenticated
    public void addBookmark(@PathParam("apartmentId") UUID apartmentId) {
        UUID userId = resolveUserId();
        userService.addBookmark(userId, apartmentId);
    }

    @DELETE
    @Path("/me/bookmarks/{apartmentId}")
    @Authenticated
    public void removeBookmark(@PathParam("apartmentId") UUID apartmentId) {
        UUID userId = resolveUserId();
        userService.removeBookmark(userId, apartmentId);
    }

    private UUID resolveUserId() {
        Object claim = jwt.getClaim("userId");
        String id = claim != null ? claim.toString() : jwt.getSubject();
        return UUID.fromString(id);
    }
}