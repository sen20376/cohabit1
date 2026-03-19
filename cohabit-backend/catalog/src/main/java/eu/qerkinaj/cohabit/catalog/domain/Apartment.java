package eu.qerkinaj.cohabit.catalog.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "apartment")
public class Apartment extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "owner_id")
    public UUID ownerId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "complex_id")
    public ResidentialComplex complex;

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Image> images;

    @Column(name = "door_number")
    public String doorNumber;

    @Column(name = "floor")
    public Integer floor;

    @Column(name = "size_sqm")
    public Double sizeSqm;

    @Column(name = "title")
    public String title;

    @Column(name = "description", length = 2000)
    public String description;

    @Column(name = "avg_rating")
    public Double avgRating = 0.0;

    @Column(name = "view_count")
    public Long viewCount = 0L;

    @Column(name = "active")
    public boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;
}
