package com.massi.mvplogement.logement;

import com.massi.mvplogement.logement.dto.CreateLogementRequest;
import com.massi.mvplogement.user.User;
import com.massi.mvplogement.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.massi.mvplogement.common.ForbiddenException;
import com.massi.mvplogement.common.NotFoundException;
import com.massi.mvplogement.logement.dto.UpdateLogementRequest;

@Service
public class LogementService {

    private final LogementRepository logementRepository;
    private final UserRepository userRepository;

    public LogementService(LogementRepository logementRepository, UserRepository userRepository) {
        this.logementRepository = logementRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Logement create(CreateLogementRequest req, Authentication auth) {
        String email = auth.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));

        Logement logement = LogementBuilder
                .forOwner(owner)
                .fromCreateRequest(req)
                .build();

        return logementRepository.save(logement);
    }

    @Transactional
    public Logement update(Long id, UpdateLogementRequest req, Authentication auth) {
        Logement logement = logementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Logement not found"));

        String email = auth.getName();
        if (!logement.getOwner().getEmail().equals(email)) {
            throw new ForbiddenException("You are not the owner of this logement");
        }

        Logement updated = LogementBuilder
                .forOwner(logement.getOwner())
                .applyUpdate(req)
                .build();

        updated.setId(logement.getId());

        return logementRepository.save(updated);
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        Logement logement = logementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Logement not found"));

        String email = auth.getName();
        if (!logement.getOwner().getEmail().equals(email)) {
            throw new ForbiddenException("You are not the owner of this logement");
        }

        logementRepository.delete(logement);
    }
}