package com.massi.mvplogement.logement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LogementRepository extends JpaRepository<Logement, Long> {

    @Query("""
        select l
        from Logement l
        join fetch l.owner
        where l.id = :id
    """)
    Optional<Logement> findByIdWithOwner(@Param("id") Long id);

    @Query("""
        select l
        from Logement l
        join fetch l.owner
        order by l.id desc
    """)
    List<Logement> findAllWithOwner();
}