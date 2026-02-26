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

    @Query("""
        select ep2
        from ExchangePeriod ep1,
             ExchangePeriod ep2
        
        join ep1.logement l1
        join l1.owner o1
        
        join fetch ep2.logement l2
        join fetch l2.owner o2
        
        where ep1.id = :periodId
          and o1.email = :email
        
          and ep1.status = 'OPEN'
          and ep2.status = 'OPEN'
        
          and o2.id <> o1.id
        
          and lower(trim(l2.city)) = lower(trim(ep1.wantCity))
          and lower(trim(ep2.wantCity)) = lower(trim(l1.city))
        
          and ep1.startDate <= ep2.endDate
          and ep2.startDate <= ep1.endDate
        
        order by ep2.createdAt desc
""")
    List<ExchangePeriod> findMatchesForPeriod(@Param("periodId") Long periodId,
                                              @Param("email") String email);
}