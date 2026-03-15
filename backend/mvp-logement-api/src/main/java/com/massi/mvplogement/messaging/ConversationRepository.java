package com.massi.mvplogement.messaging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByExchangeRequest_Id(Long requestId);

    @Query("""
        select c from Conversation c
        join fetch c.exchangeRequest er
        join fetch er.fromPeriod fp
        join fetch fp.logement fl
        join fetch fl.owner fo
        join fetch er.toPeriod tp
        join fetch tp.logement tl
        join fetch tl.owner towner
        where c.id = :id
    """)
    Optional<Conversation> findDetailedById(Long id);

    @Query("""
        select c from Conversation c
        join fetch c.exchangeRequest er
        join fetch er.fromPeriod fp
        join fetch fp.logement fl
        join fetch fl.owner fo
        join fetch er.toPeriod tp
        join fetch tp.logement tl
        join fetch tl.owner towner
    """)
    List<Conversation> findAllDetailed();
}