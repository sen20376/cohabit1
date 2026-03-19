package eu.qerkinaj.cohabit.catalog.client;

import eu.qerkinaj.cohabit.catalog.dto.RatingDTO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import java.util.UUID;

@RegisterRestClient(configKey = "rating-api")
public interface RatingClient {

    @GET
    @Path("/api/v1/ratings/target/{id}")
    List<RatingDTO> getRatings(@PathParam("id") UUID targetId);
}
