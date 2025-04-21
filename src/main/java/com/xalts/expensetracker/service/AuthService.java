package com.xalts.expensetracker.service;

import com.xalts.expensetracker.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.xalts.expensetracker.dto.JwtResponse;
import com.xalts.expensetracker.dto.LoginRequest;
import com.xalts.expensetracker.dto.RegisterRequest;
import com.xalts.expensetracker.entity.User;
import com.xalts.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public JwtResponse register(RegisterRequest request) {
        Optional<User> existing = userRepository.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            throw new RuntimeException("User already exists with email: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return new JwtResponse(token);
    }

    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new JwtResponse(token);
    }
}
