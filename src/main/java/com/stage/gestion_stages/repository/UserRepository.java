package com.stage.gestion_stages.repository;

import com.stage.gestion_stages.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Méthode pour trouver un utilisateur par email
    Optional<User> findByEmail(String email);

    // Spring Data JPA génère automatiquement :
    // - save(user) : Ajouter/modifier
    // - findById(id) : Trouver par ID
    // - findAll() : Voir tous
    // - deleteById(id) : Supprimer
    // - count() : Compter
}