package com.skillbridge.service;
import com.skillbridge.security.JwtService;
import com.skillbridge.dto.LoginRequest;
import com.skillbridge.dto.LoginResponse;
import com.skillbridge.dto.RegisterRequest;
import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    // Register New User
    public String register(RegisterRequest request) {

        // Check whether email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already registered!";
        }

        // Create a new User entity
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setCollege(request.getCollege());

        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save user
        userRepository.save(user);

        return "Registration Successful!";
    }

    // Login User
    public LoginResponse login(LoginRequest request) {

        System.out.println("========== LOGIN DEBUG ==========");
        System.out.println("Email received: [" + request.getEmail() + "]");
        System.out.println("Password received: [" +
                (request.getPassword() == null ? "NULL" : "PROVIDED") + "]");

        Optional<User> optionalUser =
                userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            System.out.println("USER NOT FOUND FOR EMAIL: [" +
                    request.getEmail() + "]");
            throw new RuntimeException("User not found!");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            System.out.println("PASSWORD DOES NOT MATCH");
            throw new RuntimeException("Invalid password!");
        }

        String token = jwtService.generateToken(user.getEmail());

        System.out.println("LOGIN SUCCESS FOR: " + user.getEmail());

        return new LoginResponse(token);
    }
}