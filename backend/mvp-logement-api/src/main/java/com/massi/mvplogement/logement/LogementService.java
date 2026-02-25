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
import org.springframework.transaction.annotation.Transactional;

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
        // Dans ton JwtAuthFilter, auth.getName() = email
        String email = auth.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));

        Logement l = new Logement();
        l.setOwner(owner);
        l.setTitle(req.title());
        l.setCity(req.city());
        l.setDescription(req.description());

        return logementRepository.save(l);
    }

    @Transactional
    public Logement update(Long id, UpdateLogementRequest req, Authentication auth) {
        Logement logement = logementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Logement not found"));

        String email = auth.getName();
        if (!logement.getOwner().getEmail().equals(email)) {
            throw new ForbiddenException("You are not the owner of this logement");
        }

        logement.setTitle(req.title());
        logement.setCity(req.city());
        logement.setDescription(req.description());

        return logementRepository.save(logement);
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