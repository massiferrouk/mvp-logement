package com.massi.mvplogement.exchange;

import com.massi.mvplogement.exchange.dto.CreateExchangeRequestRequest;
import com.massi.mvplogement.exchange.dto.ExchangeRequestResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exchange-requests")
public class ExchangeRequestController {

    private final ExchangeRequestService service;
    private final ExchangeRequestRepository repository;

    public ExchangeRequestController(ExchangeRequestService service, ExchangeRequestRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExchangeRequestResponse create(@Valid @RequestBody CreateExchangeRequestRequest req, Authentication auth) {
        ExchangeRequest saved = service.create(req, auth);
        return toResponse(saved);
    }

    @GetMapping("/inbox")
    public List<ExchangeRequestResponse> inbox(Authentication auth) {
        return repository.findInbox(auth.getName()).stream().map(this::toResponse).toList();
    }

    @GetMapping("/outbox")
    public List<ExchangeRequestResponse> outbox(Authentication auth) {
        return repository.findOutbox(auth.getName()).stream().map(this::toResponse).toList();
    }

    @PostMapping("/{id}/accept")
    public ExchangeRequestResponse accept(@PathVariable Long id, Authentication auth) {
        return toResponse(service.accept(id, auth));
    }

    @PostMapping("/{id}/reject")
    public ExchangeRequestResponse reject(@PathVariable Long id, Authentication auth) {
        return toResponse(service.reject(id, auth));
    }

    private ExchangeRequestResponse toResponse(ExchangeRequest er) {
        var fp = er.getFromPeriod();
        var tp = er.getToPeriod();

        var fpl = fp.getLogement();
        var tpl = tp.getLogement();

        return new ExchangeRequestResponse(
                er.getId(),
                er.getStatus(),
                er.getCreatedAt(),

                fp.getId(),
                fpl.getId(),
                fpl.getCity(),
                fp.getWantCity(),
                fp.getStartDate(),
                fp.getEndDate(),
                fpl.getOwner().getEmail(),

                tp.getId(),
                tpl.getId(),
                tpl.getCity(),
                tp.getWantCity(),
                tp.getStartDate(),
                tp.getEndDate(),
                tpl.getOwner().getEmail()
        );
    }
}