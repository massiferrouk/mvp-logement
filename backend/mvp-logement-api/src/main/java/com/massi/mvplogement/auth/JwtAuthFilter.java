package com.massi.mvplogement.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Jws<Claims> jws = jwtService.parseToken(token);
                Claims claims = jws.getPayload();

                String userId = claims.getSubject(); // sub=userId
                String email = claims.get("email", String.class);

                var auth = new UsernamePasswordAuthenticationToken(
                        email, null, List.of()
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                // (Plus tard, on mettra userId dans un principal custom)
            } catch (Exception e) {
                System.out.println("JWT ERROR on " + request.getMethod() + " " + request.getRequestURI()
                        + " -> " + e.getClass().getSimpleName() + " : " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}