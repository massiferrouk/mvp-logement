package com.massi.mvplogement.exchange;

import com.massi.mvplogement.exchange.dto.CreateExchangePeriodRequest;
import com.massi.mvplogement.exchange.dto.ExchangePeriodResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exchange-periods")
public class ExchangePeriodController {

    private final ExchangePeriodService service;
    private final ExchangePeriodRepository repository;

    public ExchangePeriodController(ExchangePeriodService service, ExchangePeriodRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExchangePeriodResponse create(@Valid @RequestBody CreateExchangePeriodRequest req, Authentication auth) {
        ExchangePeriod saved = service.create(req, auth);
        return toResponse(saved);
    }

    @GetMapping("/me")
    public List<ExchangePeriodResponse> myPeriods(Authentication auth) {
        String email = auth.getName();
        return repository.findMine(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ExchangePeriodResponse toResponse(ExchangePeriod p) {
        return new ExchangePeriodResponse(
                p.getId(),
                p.getLogement().getId(),
                p.getLogement().getCity(), // haveCity
                p.getWantCity(),
                p.getStartDate(),
                p.getEndDate(),
                p.getStatus(),
                p.getCreatedAt()
        );
    }
}