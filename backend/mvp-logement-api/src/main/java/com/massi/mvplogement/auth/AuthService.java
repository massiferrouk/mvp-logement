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

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
    public User login(String emailRaw, String passwordRaw) {
        String email = emailRaw.trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(passwordRaw, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}