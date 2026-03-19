package eu.qerkinaj.cohabit.catalog.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "residential_complex")
public class ResidentialComplex extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "owner_id", nullable = false)
    public UUID ownerId;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "street_name")
    public String street;

    @Column(name = "house_number")
    public String houseNumber;

    @Column(name = "zip_code")
    public String zipCode;

    @Column(name = "city")
    public String city;

    @Column(columnDefinition = "geometry(Point, 4326)")
    public Point location;

    @ManyToOne
    @JoinColumn(name = "geo_region_id")
    public GeoRegion geoRegion;

    @OneToMany(mappedBy = "complex", cascade = CascadeType.ALL)
    public List<Apartment> apartments;

    @OneToMany(mappedBy = "complex", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Image> images;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;

    @Column(name = "avg_rating")
    public Double avgRating = 0.0;
}
