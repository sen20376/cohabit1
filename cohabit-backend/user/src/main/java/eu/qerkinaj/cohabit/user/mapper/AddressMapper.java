package eu.qerkinaj.cohabit.user.mapper;

import eu.qerkinaj.cohabit.user.domain.Address;
import eu.qerkinaj.cohabit.user.dto.AddressDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jakarta-cdi")
public interface AddressMapper {

    AddressDTO toDto(Address address);

}
