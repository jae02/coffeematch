package com.coffeematch.backend.controller;

import com.coffeematch.backend.dto.AuthDto;
import com.coffeematch.backend.entity.User;
import com.coffeematch.backend.repository.UserRepository;
import com.coffeematch.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto.LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());

            return ResponseEntity.ok(new AuthDto.JwtResponse(token, user.getNickname(), user.getRole()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody AuthDto.SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname(),
                "ROLE_USER");

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }
}
