package eu.qerkinaj.cohabit.catalog.resource;

import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

public abstract class CatalogResourceBase {

    @Inject
    JsonWebToken jwt;

    protected String getUserIdSafe() {
        try {
            String subject = jwt.getSubject();
            return subject != null ? subject : "Anonymous";
        } catch (Exception e) {
            return "Anonymous";
        }
    }
}
