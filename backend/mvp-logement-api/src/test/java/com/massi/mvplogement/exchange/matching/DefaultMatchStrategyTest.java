package com.massi.mvplogement.exchange.matching;

import com.massi.mvplogement.exchange.ExchangePeriod;
import com.massi.mvplogement.logement.Logement;
import com.massi.mvplogement.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultMatchStrategy")
class DefaultMatchStrategyTest {

    private DefaultMatchStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DefaultMatchStrategy();
    }

    private static Logement logement(String city) {
        Logement l = new Logement();
        l.setCity(city);
        return l;
    }

    private static ExchangePeriod period(Logement logement, String wantCity, LocalDate start, LocalDate end) {
        ExchangePeriod p = new ExchangePeriod();
        p.setLogement(logement);
        p.setWantCity(wantCity);
        p.setStartDate(start);
        p.setEndDate(end);
        return p;
    }

    @Nested
    @DisplayName("isMatch")
    class IsMatch {

        @Test
        @DisplayName("retourne true quand villes croisées et dates se chevauchent")
        void match_whenCitiesSwapAndDatesOverlap() {
            LocalDate d1 = LocalDate.of(2025, 6, 1);
            LocalDate d2 = LocalDate.of(2025, 6, 15);
            LocalDate d3 = LocalDate.of(2025, 6, 10);
            LocalDate d4 = LocalDate.of(2025, 6, 20);

            ExchangePeriod from = period(logement("Bordeaux"), "Paris", d1, d2);
            ExchangePeriod to = period(logement("Paris"), "Bordeaux", d3, d4);

            assertTrue(strategy.isMatch(from, to));
        }

        @Test
        @DisplayName("retourne false si les villes ne correspondent pas (from veut Paris, to est à Lyon)")
        void noMatch_whenCitiesDoNotSwap() {
            LocalDate d1 = LocalDate.of(2025, 6, 1);
            LocalDate d2 = LocalDate.of(2025, 6, 15);

            ExchangePeriod from = period(logement("Bordeaux"), "Paris", d1, d2);
            ExchangePeriod to = period(logement("Lyon"), "Bordeaux", d1, d2);

            assertFalse(strategy.isMatch(from, to));
        }

        @Test
        @DisplayName("retourne false si les dates ne se chevauchent pas")
        void noMatch_whenDatesDoNotOverlap() {
            LocalDate fromStart = LocalDate.of(2025, 6, 1);
            LocalDate fromEnd = LocalDate.of(2025, 6, 10);
            LocalDate toStart = LocalDate.of(2025, 6, 15);
            LocalDate toEnd = LocalDate.of(2025, 6, 25);

            ExchangePeriod from = period(logement("Bordeaux"), "Paris", fromStart, fromEnd);
            ExchangePeriod to = period(logement("Paris"), "Bordeaux", toStart, toEnd);

            assertFalse(strategy.isMatch(from, to));
        }

        @Test
        @DisplayName("normalise les villes (casse et espaces)")
        void match_normalizesCityCaseAndSpaces() {
            LocalDate d1 = LocalDate.of(2025, 6, 1);
            LocalDate d2 = LocalDate.of(2025, 6, 15);

            ExchangePeriod from = period(logement("  BORDEAUX  "), "  paris  ", d1, d2);
            ExchangePeriod to = period(logement("Paris"), "Bordeaux", d1, d2);

            assertTrue(strategy.isMatch(from, to));
        }

        @Test
        @DisplayName("gère ville null comme chaîne vide")
        void match_handlesNullCity() {
            LocalDate d1 = LocalDate.of(2025, 6, 1);
            LocalDate d2 = LocalDate.of(2025, 6, 15);

            Logement lNull = logement(null);
            ExchangePeriod from = period(lNull, "", d1, d2);
            ExchangePeriod to = period(logement(""), "", d1, d2);

            assertTrue(strategy.isMatch(from, to));
        }
    }
}
