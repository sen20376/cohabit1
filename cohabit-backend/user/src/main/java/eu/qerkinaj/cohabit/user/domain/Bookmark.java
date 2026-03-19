package eu.qerkinaj.cohabit.user.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bookmarks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "apartment_id"})
})
public class Bookmark extends PanacheEntityBase {

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    public UUID userId;

    @Column(name = "apartment_id", nullable = false, columnDefinition = "uuid")
    public UUID apartmentId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    public static List<Bookmark> findByUserId(UUID userId) {
        return list("userId", userId);
    }

    public static Bookmark findByUserAndApartment(UUID userId, UUID apartmentId) {
        return find("userId = ?1 AND apartmentId = ?2", userId, apartmentId).firstResult();
    }
}
