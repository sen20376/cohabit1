package eu.qerkinaj.cohabit.catalog.mapper;

import eu.qerkinaj.cohabit.catalog.domain.Apartment;
import eu.qerkinaj.cohabit.catalog.domain.GeoRegion;
import eu.qerkinaj.cohabit.catalog.domain.ResidentialComplex;
import eu.qerkinaj.cohabit.catalog.dto.ApartmentDTO;
import eu.qerkinaj.cohabit.catalog.dto.GeoRegionDTO;
import eu.qerkinaj.cohabit.catalog.dto.ResidentialComplexDTO;
import eu.qerkinaj.cohabit.catalog.enums.GeoLevel;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-19T22:47:04+0100",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@ApplicationScoped
public class CatalogMapperImpl implements CatalogMapper {

    @Override
    public ResidentialComplexDTO toDTO(ResidentialComplex entity) {
        if ( entity == null ) {
            return null;
        }

        UUID id = null;
        UUID ownerId = null;
        String district = null;
        Double latitude = null;
        Double longitude = null;
        List<String> imageUrls = null;
        String name = null;

        id = entity.id;
        ownerId = entity.ownerId;
        district = entityGeoRegionName( entity );
        latitude = extractLat( entity.location );
        longitude = extractLon( entity.location );
        imageUrls = mapImages( entity.images );
        name = entity.name;

        String address = entity.street + " " + entity.houseNumber + ", " + entity.zipCode + " " + entity.city;
        Double averageRating = null;

        ResidentialComplexDTO residentialComplexDTO = new ResidentialComplexDTO( id, ownerId, name, address, district, latitude, longitude, averageRating, imageUrls );

        return residentialComplexDTO;
    }

    @Override
    public List<ResidentialComplexDTO> toComplexDTOs(List<ResidentialComplex> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ResidentialComplexDTO> list = new ArrayList<ResidentialComplexDTO>( entities.size() );
        for ( ResidentialComplex residentialComplex : entities ) {
            list.add( toDTO( residentialComplex ) );
        }

        return list;
    }

    @Override
    public ApartmentDTO toDTO(Apartment entity) {
        if ( entity == null ) {
            return null;
        }

        UUID id = null;
        UUID complexId = null;
        UUID ownerId = null;
        List<String> imageUrls = null;
        String title = null;
        String doorNumber = null;
        Integer floor = null;
        Double sizeSqm = null;
        String description = null;
        Double avgRating = null;
        Long viewCount = null;
        boolean active = false;

        id = entity.id;
        complexId = entityComplexId( entity );
        ownerId = entity.ownerId;
        imageUrls = mapImages( entity.images );
        title = entity.title;
        doorNumber = entity.doorNumber;
        floor = entity.floor;
        sizeSqm = entity.sizeSqm;
        description = entity.description;
        avgRating = entity.avgRating;
        viewCount = entity.viewCount;
        active = entity.active;

        ApartmentDTO apartmentDTO = new ApartmentDTO( id, complexId, ownerId, title, doorNumber, floor, sizeSqm, description, avgRating, viewCount, active, imageUrls );

        return apartmentDTO;
    }

    @Override
    public List<ApartmentDTO> toApartmentDTOs(List<Apartment> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ApartmentDTO> list = new ArrayList<ApartmentDTO>( entities.size() );
        for ( Apartment apartment : entities ) {
            list.add( toDTO( apartment ) );
        }

        return list;
    }

    @Override
    public GeoRegionDTO toDTO(GeoRegion entity) {
        if ( entity == null ) {
            return null;
        }

        String parentCode = null;
        String code = null;
        String name = null;
        GeoLevel level = null;

        parentCode = entityParentCode( entity );
        code = entity.code;
        name = entity.name;
        level = entity.level;

        GeoRegionDTO geoRegionDTO = new GeoRegionDTO( code, name, level, parentCode );

        return geoRegionDTO;
    }

    private String entityGeoRegionName(ResidentialComplex residentialComplex) {
        GeoRegion geoRegion = residentialComplex.geoRegion;
        if ( geoRegion == null ) {
            return null;
        }
        return geoRegion.name;
    }

    private UUID entityComplexId(Apartment apartment) {
        ResidentialComplex complex = apartment.complex;
        if ( complex == null ) {
            return null;
        }
        return complex.id;
    }

    private String entityParentCode(GeoRegion geoRegion) {
        GeoRegion parent = geoRegion.parent;
        if ( parent == null ) {
            return null;
        }
        return parent.code;
    }
}
