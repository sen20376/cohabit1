package eu.qerkinaj.cohabit.common.auth;

import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

//@IfBuildProfile("dev")
//@ApplicationScoped
public class DevAuthMechanism implements HttpAuthenticationMechanism {

    private static final Logger log = LoggerFactory.getLogger(DevAuthMechanism.class);

    private static final String BEARER = "Bearer ";

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {

        String header = context.request().headers().get(HttpHeaders.AUTHORIZATION);
        String token;

        if (header != null && header.startsWith(BEARER)) {
            token = header.substring(BEARER.length());
        } else {
            token = context.request().getParam("user");
        }

        log.info("Token for authentication (header or user param): {}", token);

        if (token != null) {
            return identityProviderManager.authenticate(
                    new TokenAuthenticationRequest(new TokenCredential(token, BEARER))
            );
        }

        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(new ChallengeData(
                401,
                "WWW-Authenticate",
                BEARER.trim() + " realm=\"dev\""
        ));
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Set.of(TokenAuthenticationRequest.class);
    }

    @Override
    public Uni<HttpCredentialTransport> getCredentialTransport(RoutingContext context) {
        return Uni.createFrom().item(
                new HttpCredentialTransport(HttpCredentialTransport.Type.AUTHORIZATION, "Bearer")
        );
    }
}
