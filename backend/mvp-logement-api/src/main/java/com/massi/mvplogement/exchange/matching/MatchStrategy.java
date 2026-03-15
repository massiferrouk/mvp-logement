package com.massi.mvplogement.exchange.matching;

import com.massi.mvplogement.exchange.ExchangePeriod;

public interface MatchStrategy {

    boolean isMatch(ExchangePeriod from, ExchangePeriod to);
}

