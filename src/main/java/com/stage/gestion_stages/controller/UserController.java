package com.stage.gestion_stages.controller;

import com.stage.gestion_stages.model.User;
import com.stage.gestion_stages.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 1. Créer un utilisateur - POST /api/users
    @PostMapping
    public ResponseEntity<User> creerUtilisateur(@RequestBody User user) {
        // Vérifie si l'email existe déjà
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict
        }

        User nouveauUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(nouveauUser); // 201 Created
    }

    // 2. Obtenir tous les utilisateurs - GET /api/users
    @GetMapping
    public ResponseEntity<List<User>> obtenirTousLesUtilisateurs() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users); // 200 OK
    }

    // 3. Obtenir un utilisateur par ID - GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<User> obtenirUtilisateurParId(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get()); // 200 OK
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    // 4. Obtenir un utilisateur par email - GET /api/users/email/{email}
    @GetMapping("/email/{email}")
    public ResponseEntity<User> obtenirUtilisateurParEmail(@PathVariable String email) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. Supprimer un utilisateur - DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerUtilisateur(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}