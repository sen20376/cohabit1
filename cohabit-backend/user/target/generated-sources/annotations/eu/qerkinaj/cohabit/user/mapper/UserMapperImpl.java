package eu.qerkinaj.cohabit.user.mapper;

import eu.qerkinaj.cohabit.user.domain.User;
import eu.qerkinaj.cohabit.user.dto.AddressDTO;
import eu.qerkinaj.cohabit.user.dto.UserDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-19T22:53:11+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.9 (Azul Systems, Inc.)"
)
@ApplicationScoped
public class UserMapperImpl implements UserMapper {

    @Inject
    private AddressMapper addressMapper;

    @Override
    public UserDTO toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UUID id = null;
        String email = null;
        Set<String> roles = null;
        AddressDTO address = null;

        id = user.id;
        email = user.email;
        Set<String> set = user.roles;
        if ( set != null ) {
            roles = new LinkedHashSet<String>( set );
        }
        address = addressMapper.toDto( user.address );

        UserDTO userDTO = new UserDTO( id, email, address, roles );

        return userDTO;
    }
}
