package com.massi.mvplogement.auth;

import com.massi.mvplogement.auth.dto.LoginResponse;
import com.massi.mvplogement.auth.dto.RegisterRequest;
import com.massi.mvplogement.auth.dto.LoginRequest;
import com.massi.mvplogement.auth.dto.RegisterResponse;
import com.massi.mvplogement.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints d'authentification des utilisateurs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Créer un compte utilisateur",
            description = "Permet à un nouvel utilisateur de créer un compte dans l'application."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {

        User user = authService.register(req);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(user.getId(), user.getEmail()));
    }

    @Operation(
            summary = "Connexion utilisateur",
            description = "Authentifie un utilisateur et retourne un token JWT permettant d'accéder aux endpoints sécurisés."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion réussie"),
            @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    })
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {

        String token = authService.loginAndCreateToken(req.email(), req.password());

        return new LoginResponse(token, authService.tokenExpiresIn());
    }
}