package com.massi.mvplogement.exchange;

import com.massi.mvplogement.common.BadRequestException;
import com.massi.mvplogement.common.ForbiddenException;
import com.massi.mvplogement.common.NotFoundException;
import com.massi.mvplogement.exchange.dto.CreateExchangePeriodRequest;
import com.massi.mvplogement.logement.Logement;
import com.massi.mvplogement.logement.LogementRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExchangePeriodService {

    private final ExchangePeriodRepository repository;
    private final LogementRepository logementRepository;

    public ExchangePeriodService(ExchangePeriodRepository repository, LogementRepository logementRepository) {
        this.repository = repository;
        this.logementRepository = logementRepository;
    }

    @Transactional
    public ExchangePeriod create(CreateExchangePeriodRequest req, Authentication auth) {
        String email = auth.getName();

        Logement logement = logementRepository.findById(req.logementId())
                .orElseThrow(() -> new NotFoundException("Logement not found"));

        if (!logement.getOwner().getEmail().equals(email)) {
            throw new ForbiddenException("You are not the owner of this logement");
        }

        if (req.startDate().isAfter(req.endDate())) {
            throw new BadRequestException("startDate must be <= endDate");
        }

        ExchangePeriod p = new ExchangePeriod();
        p.setLogement(logement);
        p.setWantCity(req.wantCity());
        p.setStartDate(req.startDate());
        p.setEndDate(req.endDate());
        p.setStatus("OPEN");

        return repository.save(p);
    }
}