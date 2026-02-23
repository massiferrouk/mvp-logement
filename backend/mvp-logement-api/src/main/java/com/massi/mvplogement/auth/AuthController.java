package com.massi.mvplogement.auth;

import com.massi.mvplogement.auth.dto.RegisterRequest;
import com.massi.mvplogement.auth.dto.LoginRequest;
import com.massi.mvplogement.auth.dto.RegisterResponse;
import com.massi.mvplogement.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        User user = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(user.getId(), user.getEmail()));
    }

    @PostMapping("/login")
    public RegisterResponse login(@Valid @RequestBody LoginRequest req) {
        User user = authService.login(req.email(), req.password());
        return new RegisterResponse(user.getId(), user.getEmail());
    }
}