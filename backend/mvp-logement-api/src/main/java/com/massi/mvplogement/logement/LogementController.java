package com.massi.mvplogement.logement;

import com.massi.mvplogement.logement.dto.CreateLogementRequest;
import com.massi.mvplogement.logement.dto.LogementResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.massi.mvplogement.logement.dto.UpdateLogementRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/logements")
public class LogementController {

    private final LogementRepository logementRepository;
    private final LogementService logementService;

    public LogementController(LogementRepository logementRepository, LogementService logementService) {
        this.logementRepository = logementRepository;
        this.logementService = logementService;
    }

    // ✅ PUBLIC
    @GetMapping
    public List<LogementResponse> list() {
        return logementRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // ✅ PUBLIC
    @GetMapping("/{id}")
    public LogementResponse get(@PathVariable Long id) {
        Logement l = logementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Logement not found"));
        return toResponse(l);
    }

    // 🔐 PROTECTED (JWT)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LogementResponse create(@Valid @RequestBody CreateLogementRequest req, Authentication auth) {
        Logement saved = logementService.create(req, auth);
        return toResponse(saved);
    }

    private LogementResponse toResponse(Logement l) {
        // Attention LAZY: on lit owner seulement ici (ça peut marcher car transaction ouverte sur findAll)
        // Pour MVP c’est OK. On optimisera plus tard si besoin.
        return new LogementResponse(
                l.getId(),
                l.getTitle(),
                l.getCity(),
                l.getDescription(),
                l.getCreatedAt(),
                l.getOwner().getId(),
                l.getOwner().getEmail()
        );
    }

    @PutMapping("/{id}")
    public LogementResponse update(@PathVariable Long id,
                                   @Valid @RequestBody UpdateLogementRequest req,
                                   Authentication auth) {
        Logement updated = logementService.update(id, req, auth);
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        logementService.delete(id, auth);
    }
}