package com.stage.gestion_stages.controller;

import com.stage.gestion_stages.dto.AuthResponse;
import com.stage.gestion_stages.dto.LoginRequest;
import com.stage.gestion_stages.model.User;
import com.stage.gestion_stages.repository.UserRepository;
import com.stage.gestion_stages.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.stage.gestion_stages.dto.UserDTO;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Inscription (Register)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cet email est déjà utilisé");
        }

        // Hacher le mot de passe avec BCrypt
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Sauvegarder l'utilisateur
        User newUser = userRepository.save(user);

        // Générer un token JWT
        String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getRole().name());

        // Retourner la réponse avec le token
        AuthResponse response = new AuthResponse(
                token,
                newUser.getId(),
                newUser.getEmail(),
                newUser.getNom(),
                newUser.getPrenom(),
                newUser.getRole().name()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Connexion (Login)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Chercher l'utilisateur par email
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect");
        }

        User user = userOpt.get();

        // Vérifier le mot de passe avec BCrypt
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect");
        }

        // Générer un token JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // Retourner la réponse avec le token
        AuthResponse response = new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getRole().name()
        );

        return ResponseEntity.ok(response);
    }

    // Récupérer l'utilisateur connecté
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        // Extraire le token du header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token manquant");
        }

        String token = authHeader.substring(7); // Enlever "Bearer "

        // Extraire l'email du token
        String email = jwtUtil.extractUsername(token);

        // Chercher l'utilisateur
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable");
        }
        // Créer un DTO sans le mot de passe
        UserDTO userDTO = new UserDTO(
                user.get().getId(),
                user.get().getEmail(),
                user.get().getNom(),
                user.get().getPrenom(),
                user.get().getRole().name() // Convertir Enum en String
        );

        return ResponseEntity.ok(userDTO);
    }
}