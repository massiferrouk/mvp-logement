package com.massi.mvplogement.auth.dto;

public record LoginResponse(String token, long expiresIn) {}