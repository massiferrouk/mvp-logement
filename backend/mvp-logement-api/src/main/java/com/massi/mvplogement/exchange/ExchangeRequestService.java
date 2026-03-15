package com.massi.mvplogement.exchange;

import com.massi.mvplogement.common.BadRequestException;
import com.massi.mvplogement.common.ForbiddenException;
import com.massi.mvplogement.common.NotFoundException;
import com.massi.mvplogement.exchange.dto.CreateExchangeRequestRequest;
import com.massi.mvplogement.messaging.Conversation;
import com.massi.mvplogement.messaging.ConversationRepository;
import com.massi.mvplogement.exchange.matching.MatchStrategy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExchangeRequestService {

    private final ExchangeRequestRepository requestRepo;
    private final ExchangePeriodRepository periodRepo;
    private final ConversationRepository conversationRepository;
    private final MatchStrategy matchStrategy;

    public ExchangeRequestService(ExchangeRequestRepository requestRepo,
                                  ExchangePeriodRepository periodRepo,
                                  ConversationRepository conversationRepository,
                                  MatchStrategy matchStrategy) {
        this.requestRepo = requestRepo;
        this.periodRepo = periodRepo;
        this.conversationRepository = conversationRepository;
        this.matchStrategy = matchStrategy;
    }

    @Transactional
    public ExchangeRequest create(CreateExchangeRequestRequest req, Authentication auth) {
        String email = auth.getName();

        if (req.fromPeriodId().equals(req.toPeriodId())) {
            throw new BadRequestException("fromPeriodId and toPeriodId must be different");
        }

        ExchangePeriod from = periodRepo.findById(req.fromPeriodId())
                .orElseThrow(() -> new NotFoundException("fromPeriod not found"));
        ExchangePeriod to = periodRepo.findById(req.toPeriodId())
                .orElseThrow(() -> new NotFoundException("toPeriod not found"));

        // owner-only: je peux envoyer seulement depuis MA période (owner du logement de from)
        if (!from.getLogement().getOwner().getEmail().equals(email)) {
            throw new ForbiddenException("You are not the owner of fromPeriod");
        }

        // pas à soi-même
        if (to.getLogement().getOwner().getEmail().equals(email)) {
            throw new BadRequestException("You cannot request an exchange with yourself");
        }

        // status open
        if (!"OPEN".equals(from.getStatus()) || !"OPEN".equals(to.getStatus())) {
            throw new BadRequestException("Both periods must be OPEN");
        }

        // Optionnel mais logique MVP : vérifier que c'est bien un match (ville inversée + overlap)
        if (!matchStrategy.isMatch(from, to)) {
            throw new BadRequestException("Periods are not compatible (no match)");
        }

        // pas de doublon
        if (requestRepo.existsByFromPeriod_IdAndToPeriod_Id(req.fromPeriodId(), req.toPeriodId())) {
            throw new BadRequestException("Request already exists");
        }

        ExchangeRequest er = new ExchangeRequest();
        er.setFromPeriod(from);
        er.setToPeriod(to);
        er.setStatus("PENDING");

        return requestRepo.save(er);
    }

    @Transactional
    public ExchangeRequest accept(Long requestId, Authentication auth) {
        String email = auth.getName();

        ExchangeRequest er = requestRepo.findByIdFull(requestId)
                .orElseThrow(() -> new NotFoundException("Exchange request not found"));

        // seul le destinataire (owner du logement de toPeriod) peut accepter
        if (!er.getToPeriod().getLogement().getOwner().getEmail().equals(email)) {
            throw new ForbiddenException("You are not allowed to accept this request");
        }

        if (!"PENDING".equals(er.getStatus())) {
            throw new BadRequestException("Only PENDING requests can be accepted");
        }

        er.setStatus("ACCEPTED");

        Conversation conversation = new Conversation();
        conversation.setExchangeRequest(er);
        conversationRepository.save(conversation);

        // MVP simple : on peut fermer les périodes pour éviter d'autres échanges
        er.getFromPeriod().setStatus("CLOSED");
        er.getToPeriod().setStatus("CLOSED");

        return er; // transaction + dirty checking
    }

    @Transactional
    public ExchangeRequest reject(Long requestId, Authentication auth) {
        String email = auth.getName();

        ExchangeRequest er = requestRepo.findByIdFull(requestId)
                .orElseThrow(() -> new NotFoundException("Exchange request not found"));

        if (!er.getToPeriod().getLogement().getOwner().getEmail().equals(email)) {
            throw new ForbiddenException("You are not allowed to reject this request");
        }

        if (!"PENDING".equals(er.getStatus())) {
            throw new BadRequestException("Only PENDING requests can be rejected");
        }

        er.setStatus("REJECTED");
        return er;
    }

}