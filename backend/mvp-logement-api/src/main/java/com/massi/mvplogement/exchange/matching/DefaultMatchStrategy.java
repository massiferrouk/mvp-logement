package com.massi.mvplogement.exchange.matching;

import com.massi.mvplogement.exchange.ExchangePeriod;
import org.springframework.stereotype.Component;

@Component
public class DefaultMatchStrategy implements MatchStrategy {

    @Override
    public boolean isMatch(ExchangePeriod from, ExchangePeriod to) {
        boolean citiesOk =
                normalize(to.getLogement().getCity()).equals(normalize(from.getWantCity()))
                        && normalize(to.getWantCity()).equals(normalize(from.getLogement().getCity()));

        boolean overlapOk =
                !from.getStartDate().isAfter(to.getEndDate())
                        && !to.getStartDate().isAfter(from.getEndDate());

        return citiesOk && overlapOk;
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}

