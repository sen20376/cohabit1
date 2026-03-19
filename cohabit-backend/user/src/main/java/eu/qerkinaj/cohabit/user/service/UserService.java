package eu.qerkinaj.cohabit.user.service;

import eu.qerkinaj.cohabit.user.domain.Bookmark;
import eu.qerkinaj.cohabit.user.domain.User;
import eu.qerkinaj.cohabit.user.dto.UserDTO;
import eu.qerkinaj.cohabit.user.mapper.UserMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    UserMapper userMapper;

    public UserDTO getUser(UUID uuid) {
        return userMapper.toDto(User.findById(uuid));
    }

    public String getUserEmail(UUID uuid) {
        return User.findById(uuid).email;
    }

    public List<UUID> getBookmarks(UUID userId) {
        return Bookmark.findByUserId(userId).stream()
                .map(b -> b.apartmentId)
                .toList();
    }

    @Transactional
    public void addBookmark(UUID userId, UUID apartmentId) {
        if (Bookmark.findByUserAndApartment(userId, apartmentId) != null) {
            throw new BadRequestException("Bereits in der Merkliste.");
        }
        Bookmark b = new Bookmark();
        b.userId = userId;
        b.apartmentId = apartmentId;
        b.persist();
    }

    @Transactional
    public void removeBookmark(UUID userId, UUID apartmentId) {
        Bookmark b = Bookmark.findByUserAndApartment(userId, apartmentId);
        if (b == null) {
            throw new NotFoundException("Eintrag nicht in der Merkliste.");
        }
        b.delete();
    }
}
