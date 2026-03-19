package eu.qerkinaj.cohabit.common.auth;

import eu.qerkinaj.cohabit.common.Role;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import java.util.Set;

//@IfBuildProfile("dev")
//@ApplicationScoped
public class DevIdentityProvider implements IdentityProvider<TokenAuthenticationRequest> {

    @Override
    public Class<TokenAuthenticationRequest> getRequestType() {
        return TokenAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(TokenAuthenticationRequest tokenAuthenticationRequest, AuthenticationRequestContext authenticationRequestContext) {

        String token = tokenAuthenticationRequest.getToken().getToken();

        String email;
        String userId;
        Set<String> roles;

        switch (token) {
            case "ADMIN":
                email = "admin@kobutsu.qerkinaj.eu";
                userId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
                roles = Set.of(Role.ADMIN, Role.USER);
                break;
            case "USER":
                email = "albin@kobutsu.qerkinaj.eu";
                userId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
                roles = Set.of(Role.USER);
                break;
            case "VENDOR":
                email = "vendor@kobutsu.qerkinaj.eu";
                userId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
                roles = Set.of(Role.VENDOR);
                break;
            default:
                email = "test@kobutsu.qerkinaj.eu";
                userId = "cccccccc-cccc-cccc-cccc-cccccccccccc";
                roles = Set.of(Role.USER);
                break;
        }

        SecurityIdentity securityIdentity = QuarkusSecurityIdentity.builder()
                .setPrincipal(() -> email)
                .addCredential(tokenAuthenticationRequest.getToken())
                .addRoles(roles)
                .addAttribute("email", email)
                .addAttribute("userId", userId)
                .build();

        return Uni.createFrom().item(securityIdentity);
    }
}
