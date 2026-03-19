package eu.qerkinaj.cohabit.user.mapper;

import eu.qerkinaj.cohabit.user.domain.Address;
import eu.qerkinaj.cohabit.user.dto.AddressDTO;
import jakarta.enterprise.context.ApplicationScoped;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-19T22:53:11+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.9 (Azul Systems, Inc.)"
)
@ApplicationScoped
public class AddressMapperImpl implements AddressMapper {

    @Override
    public AddressDTO toDto(Address address) {
        if ( address == null ) {
            return null;
        }

        String street = null;
        String city = null;
        String postalCode = null;
        String country = null;

        street = address.street;
        city = address.city;
        postalCode = address.postalCode;
        country = address.country;

        AddressDTO addressDTO = new AddressDTO( street, city, postalCode, country );

        return addressDTO;
    }
}
