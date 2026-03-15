package com.massi.mvplogement.logement;

import com.massi.mvplogement.common.ForbiddenException;
import com.massi.mvplogement.common.NotFoundException;
import com.massi.mvplogement.logement.dto.CreateLogementRequest;
import com.massi.mvplogement.logement.dto.UpdateLogementRequest;
import com.massi.mvplogement.user.User;
import com.massi.mvplogement.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogementService")
class LogementServiceTest {

    @Mock
    private LogementRepository logementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication auth;

    @InjectMocks
    private LogementService logementService;

    private static final String OWNER_EMAIL = "owner@test.com";

    @BeforeEach
    void setUp() {
        lenient().when(auth.getName()).thenReturn(OWNER_EMAIL);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("enregistre un logement et le retourne quand l'utilisateur existe")
        void savesAndReturnsLogementWhenUserFound() {
            User owner = new User(OWNER_EMAIL, "hash");
            when(userRepository.findByEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));

            CreateLogementRequest req = new CreateLogementRequest("Studio", "Bordeaux", "Desc");
            Logement saved = new Logement();
            saved.setId(1L);
            when(logementRepository.save(any(Logement.class))).thenReturn(saved);

            Logement result = logementService.create(req, auth);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(userRepository).findByEmail(OWNER_EMAIL);
            verify(logementRepository).save(any(Logement.class));
        }

        @Test
        @DisplayName("lance IllegalStateException si l'utilisateur authentifié n'existe pas en base")
        void throwsWhenUserNotFound() {
            when(userRepository.findByEmail(OWNER_EMAIL)).thenReturn(Optional.empty());

            CreateLogementRequest req = new CreateLogementRequest("Studio", "Bordeaux", "Desc");

            assertThrows(IllegalStateException.class, () -> logementService.create(req, auth));
            verify(logementRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("met à jour et sauvegarde quand le logement existe et l'utilisateur est propriétaire")
        void updatesWhenOwner() {
            User owner = new User(OWNER_EMAIL, "hash");
            Logement existing = new Logement();
            existing.setId(10L);
            existing.setOwner(owner);
            existing.setTitle("Old");
            existing.setCity("Paris");

            when(logementRepository.findById(10L)).thenReturn(Optional.of(existing));

            UpdateLogementRequest req = new UpdateLogementRequest("New Title", "Bordeaux", "New desc");
            when(logementRepository.save(any(Logement.class))).thenAnswer(inv -> inv.getArgument(0));

            Logement result = logementService.update(10L, req, auth);

            assertNotNull(result);
            verify(logementRepository).save(any(Logement.class));
        }

        @Test
        @DisplayName("lance NotFoundException si le logement n'existe pas")
        void throwsNotFoundWhenLogementMissing() {
            when(logementRepository.findById(999L)).thenReturn(Optional.empty());
            UpdateLogementRequest req = new UpdateLogementRequest("T", "Bordeaux", "D");

            assertThrows(NotFoundException.class, () -> logementService.update(999L, req, auth));
            verify(logementRepository, never()).save(any());
        }

        @Test
        @DisplayName("lance ForbiddenException si l'utilisateur n'est pas le propriétaire")
        void throwsForbiddenWhenNotOwner() {
            User other = new User("other@test.com", "hash");
            Logement existing = new Logement();
            existing.setId(10L);
            existing.setOwner(other);

            when(logementRepository.findById(10L)).thenReturn(Optional.of(existing));
            UpdateLogementRequest req = new UpdateLogementRequest("T", "Bordeaux", "D");

            assertThrows(ForbiddenException.class, () -> logementService.update(10L, req, auth));
            verify(logementRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("supprime le logement quand l'utilisateur est propriétaire")
        void deletesWhenOwner() {
            User owner = new User(OWNER_EMAIL, "hash");
            Logement existing = new Logement();
            existing.setId(5L);
            existing.setOwner(owner);

            when(logementRepository.findById(5L)).thenReturn(Optional.of(existing));

            logementService.delete(5L, auth);

            verify(logementRepository).delete(existing);
        }

        @Test
        @DisplayName("lance NotFoundException si le logement n'existe pas")
        void throwsNotFoundWhenLogementMissing() {
            when(logementRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> logementService.delete(999L, auth));
            verify(logementRepository, never()).delete(any());
        }

        @Test
        @DisplayName("lance ForbiddenException si l'utilisateur n'est pas le propriétaire")
        void throwsForbiddenWhenNotOwner() {
            User other = new User("other@test.com", "hash");
            Logement existing = new Logement();
            existing.setId(5L);
            existing.setOwner(other);

            when(logementRepository.findById(5L)).thenReturn(Optional.of(existing));

            assertThrows(ForbiddenException.class, () -> logementService.delete(5L, auth));
            verify(logementRepository, never()).delete(any());
        }
    }
}
