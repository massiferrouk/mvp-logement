package com.massi.mvplogement.exchange;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {

    boolean existsByFromPeriod_IdAndToPeriod_Id(Long fromPeriodId, Long toPeriodId);

    // outbox = demandes envoyées par moi (owner du logement de fromPeriod)
    @Query("""
        select er
        from ExchangeRequest er
        join fetch er.fromPeriod fp
        join fetch fp.logement fpl
        join fetch fpl.owner fpo
        join fetch er.toPeriod tp
        join fetch tp.logement tpl
        join fetch tpl.owner tpo
        where fpo.email = :email
        order by er.createdAt desc
    """)
    List<ExchangeRequest> findOutbox(@Param("email") String email);

    // inbox = demandes reçues par moi (owner du logement de toPeriod)
    @Query("""
        select er
        from ExchangeRequest er
        join fetch er.fromPeriod fp
        join fetch fp.logement fpl
        join fetch fpl.owner fpo
        join fetch er.toPeriod tp
        join fetch tp.logement tpl
        join fetch tpl.owner tpo
        where tpo.email = :email
        order by er.createdAt desc
    """)
    List<ExchangeRequest> findInbox(@Param("email") String email);

    @Query("""
        select er
        from ExchangeRequest er
        join fetch er.fromPeriod fp
        join fetch fp.logement fpl
        join fetch fpl.owner fpo
        join fetch er.toPeriod tp
        join fetch tp.logement tpl
        join fetch tpl.owner tpo
        where er.id = :id
    """)
    Optional<ExchangeRequest> findByIdFull(@Param("id") Long id);
}