package com.massi.mvplogement.logement;

import com.massi.mvplogement.logement.dto.CreateLogementRequest;
import com.massi.mvplogement.logement.dto.UpdateLogementRequest;
import com.massi.mvplogement.user.User;

public class LogementBuilder {

    private final Logement logement;

    private LogementBuilder(User owner) {
        this.logement = new Logement();
        this.logement.setOwner(owner);
    }

    public static LogementBuilder forOwner(User owner) {
        return new LogementBuilder(owner);
    }

    public LogementBuilder fromCreateRequest(CreateLogementRequest req) {
        this.logement.setTitle(req.title());
        this.logement.setCity(req.city());
        this.logement.setDescription(req.description());
        return this;
    }

    public LogementBuilder applyUpdate(UpdateLogementRequest req) {
        this.logement.setTitle(req.title());
        this.logement.setCity(req.city());
        this.logement.setDescription(req.description());
        return this;
    }

    public Logement build() {
        return this.logement;
    }
}

