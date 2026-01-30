package com.stage.gestion_stages.repository;

import com.stage.gestion_stages.model.InternshipOffer;
import com.stage.gestion_stages.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipOfferRepository extends JpaRepository<InternshipOffer, Long> {

    // Trouver toutes les offres d'une entreprise spécifique
    // SELECT * FROM internship_offers WHERE user_id = ?
    List<InternshipOffer> findByCreateur(User createur);

    // Trouver les offres par statut
    // SELECT * FROM internship_offers WHERE statut = ?
    List<InternshipOffer> findByStatut(InternshipOffer.Statut statut);

    // Trouver les offres par ville
    // SELECT * FROM internship_offers WHERE ville = ?
    List<InternshipOffer> findByVille(String ville);

    // Trouver les offres par titre (recherche partielle, insensible à la casse)
    // SELECT * FROM internship_offers WHERE titre LIKE %?% (case insensitive)
    List<InternshipOffer> findByTitreContainingIgnoreCase(String titre);

    // Spring Data JPA génère automatiquement :
    // - save(offer) : Créer/modifier une offre
    // - findById(id) : Trouver par ID
    // - findAll() : Toutes les offres
    // - deleteById(id) : Supprimer
    // - count() : Compter
}