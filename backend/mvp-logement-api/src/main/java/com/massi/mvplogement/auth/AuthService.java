package com.massi.mvplogement.auth;

import com.massi.mvplogement.auth.dto.RegisterRequest;
import com.massi.mvplogement.user.User;
import com.massi.mvplogement.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public User register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException();
        }

        String hash = passwordEncoder.encode(req.password());
        User user = new User(email, hash);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String loginAndCreateToken(String emailRaw, String passwordRaw) {
        String email = emailRaw.trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(passwordRaw, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return jwtService.generateToken(user.getId(), user.getEmail());
    }

    public long tokenExpiresIn() {
        return jwtService.getExpirationSeconds();
    }
}