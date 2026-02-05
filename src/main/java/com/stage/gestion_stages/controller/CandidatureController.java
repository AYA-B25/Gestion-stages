package com.stage.gestion_stages.controller;

import com.stage.gestion_stages.model.Candidature;
import com.stage.gestion_stages.model.User;
import com.stage.gestion_stages.repository.CandidatureRepository;
import com.stage.gestion_stages.repository.UserRepository;
import com.stage.gestion_stages.model.InternshipOffer;
import com.stage.gestion_stages.repository.InternshipOfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/candidatures")
public class CandidatureController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InternshipOfferRepository offerRepository;

    @Autowired
    private CandidatureRepository appRepository;

    // POST - Créer une candidature
    @PostMapping
    public ResponseEntity<?> creerApplication(@RequestBody Candidature candidature) {
        // Validation de l'offre
        if (candidature.getOffre() == null || candidature.getOffre().getId() == null) {
            return ResponseEntity.badRequest().body("L'offre est obligatoire");
        }

        // Validation du candidat
        if (candidature.getCandidat() == null || candidature.getCandidat().getId() == null) {
            return ResponseEntity.badRequest().body("Le candidat est obligatoire");
        }

        // Validation du message de motivation
        if (candidature.getMessageMotivation() == null || candidature.getMessageMotivation().isEmpty()) {
            return ResponseEntity.badRequest().body("Le message de motivation est obligatoire");
        }

        // Vérifier que le candidat existe
        Optional<User> candidat = userRepository.findById(candidature.getCandidat().getId());
        if (candidat.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Le candidat est introuvable");
        }

        // Vérifier que c'est bien un étudiant
        if (candidat.get().getRole() != User.Role.ETUDIANT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Seuls les étudiants peuvent postuler");
        }

        // Vérifier que l'offre existe
        Optional<InternshipOffer> offre = offerRepository.findById(candidature.getOffre().getId());
        if (offre.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("L'offre est introuvable");
        }

        // Vérifier que l'offre est ouverte
        if (offre.get().getStatut() != InternshipOffer.Statut.OUVERTE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Cette offre n'est plus disponible");
        }

        // Vérifier que l'étudiant n'a pas déjà postulé
        Optional<Candidature> candidatureExistante = appRepository.findByCandidatAndOffre(candidat.get(), offre.get());
        if (candidatureExistante.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Vous avez déjà postulé à cette offre");
        }

        // Associer le candidat et l'offre
        candidature.setCandidat(candidat.get());
        candidature.setOffre(offre.get());

        // Définir le statut par défaut
        if (candidature.getStatut() == null) {
            candidature.setStatut(Candidature.Statut.EN_ATTENTE);
        }

        // Définir la date de candidature
        if (candidature.getDateCandidature() == null) {
            candidature.setDateCandidature(LocalDate.now());
        }

        // Sauvegarder la candidature
        Candidature nouvelleCandidature = appRepository.save(candidature);
        return ResponseEntity.status(HttpStatus.CREATED).body(nouvelleCandidature); // ← CORRECTION
    }

    // GET - Candidature par ID
    @GetMapping("/{id}")
    public ResponseEntity<Candidature> obtenirCandidatureParId(@PathVariable Long id) {
        Optional<Candidature> candidature = appRepository.findById(id);
        return candidature.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET - Candidatures d'un étudiant
    @GetMapping("/etudiant/{id}")
    public ResponseEntity<?> obtenirCandidaturesParEtud(@PathVariable Long id) {
        Optional<User> etudiant = userRepository.findById(id);
        if (etudiant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Candidature> candidatures = appRepository.findApplicationsByCandidat(etudiant.get());
        return ResponseEntity.ok(candidatures);
    }

    // GET - Candidatures pour une offre
    @GetMapping("/offre/{id}")
    public ResponseEntity<?> obtenirCandidaturesParOffre(@PathVariable Long id) {
        Optional<InternshipOffer> offre = offerRepository.findById(id);
        if (offre.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Candidature> candidatures = appRepository.findApplicationsByOffre(offre.get());
        return ResponseEntity.ok(candidatures);
    }

    // GET - Candidatures par statut
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Candidature>> obtenirCandidaturesParStatut(@PathVariable Candidature.Statut statut) {
        List<Candidature> candidatures = appRepository.findApplicationsByStatut(statut);
        return ResponseEntity.ok(candidatures);
    }

    // PUT - Changer le statut (avec DTO)
    @PutMapping("/{id}/statut")
    public ResponseEntity<?> modifStatutApp(@PathVariable Long id, @RequestBody StatutRequest request) {
        Candidature.Statut nouveauStatut = request.getStatut();

        // Validation du statut
        if (nouveauStatut != Candidature.Statut.ACCEPTEE && nouveauStatut != Candidature.Statut.REFUSEE) {
            return ResponseEntity.badRequest().body("Le statut doit être ACCEPTEE ou REFUSEE");
        }

        // Chercher la candidature
        Optional<Candidature> candidature = appRepository.findById(id);
        if (candidature.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Mettre à jour le statut
        candidature.get().setStatut(nouveauStatut);
        Candidature candidatureMaj = appRepository.save(candidature.get());
        return ResponseEntity.ok(candidatureMaj);
    }

    // DELETE - Supprimer une candidature
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprCandidature(@PathVariable Long id) {
        if (!appRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        appRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Classe interne pour le changement de statut
    static class StatutRequest {
        private Candidature.Statut statut;

        public Candidature.Statut getStatut() {
            return statut;
        }

        public void setStatut(Candidature.Statut statut) {
            this.statut = statut;
        }
    }
}
