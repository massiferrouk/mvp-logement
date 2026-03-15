package com.massi.mvplogement.logement;

import com.massi.mvplogement.logement.dto.CreateLogementRequest;
import com.massi.mvplogement.logement.dto.UpdateLogementRequest;
import com.massi.mvplogement.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LogementBuilder")
class LogementBuilderTest {

    private static User createUser() {
        return new User("owner@test.com", "hash");
    }

    @Test
    @DisplayName("construit un Logement à partir d'un CreateLogementRequest")
    void fromCreateRequest_fillsAllFields() {
        User owner = createUser();
        CreateLogementRequest req = new CreateLogementRequest(
                "Studio Bordeaux",
                "Bordeaux",
                "Proche tram"
        );

        Logement logement = LogementBuilder
                .forOwner(owner)
                .fromCreateRequest(req)
                .build();

        assertNotNull(logement);
        assertSame(owner, logement.getOwner());
        assertEquals("Studio Bordeaux", logement.getTitle());
        assertEquals("Bordeaux", logement.getCity());
        assertEquals("Proche tram", logement.getDescription());
    }

    @Test
    @DisplayName("applyUpdate met à jour titre, ville et description")
    void applyUpdate_updatesFields() {
        User owner = createUser();
        UpdateLogementRequest req = new UpdateLogementRequest(
                "Appartement rénové",
                "Talence",
                "Calme, jardin"
        );

        Logement logement = LogementBuilder
                .forOwner(owner)
                .applyUpdate(req)
                .build();

        assertEquals("Appartement rénové", logement.getTitle());
        assertEquals("Talence", logement.getCity());
        assertEquals("Calme, jardin", logement.getDescription());
    }

    @Test
    @DisplayName("chaînage fromCreateRequest puis applyUpdate")
    void chain_fromCreateThenApplyUpdate() {
        User owner = createUser();
        CreateLogementRequest create = new CreateLogementRequest("T1", "Bordeaux", "Desc");
        UpdateLogementRequest update = new UpdateLogementRequest("T2", "Pessac", "Nouvelle desc");

        Logement logement = LogementBuilder
                .forOwner(owner)
                .fromCreateRequest(create)
                .applyUpdate(update)
                .build();

        assertEquals("T2", logement.getTitle());
        assertEquals("Pessac", logement.getCity());
        assertEquals("Nouvelle desc", logement.getDescription());
    }
}
