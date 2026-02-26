package com.massi.mvplogement.exchange;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExchangePeriodRepository extends JpaRepository<ExchangePeriod, Long> {

    @Query("""
        select ep
        from ExchangePeriod ep
        join fetch ep.logement l
        join fetch l.owner o
        where o.email = :email
        order by ep.createdAt desc
    """)
    List<ExchangePeriod> findMine(@Param("email") String email);
}