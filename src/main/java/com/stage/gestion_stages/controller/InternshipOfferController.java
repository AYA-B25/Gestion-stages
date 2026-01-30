package com.stage.gestion_stages.controller;

import com.stage.gestion_stages.model.InternshipOffer;
import com.stage.gestion_stages.repository.InternshipOfferRepository;
import com.stage.gestion_stages.model.User;
import com.stage.gestion_stages.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/offers")
public class InternshipOfferController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InternshipOfferRepository offerRepository;

    // 1. Créer une offre - POST /api/offer
    @PostMapping
    public ResponseEntity<?> creerOffre(@Valid @RequestBody InternshipOffer offer, BindingResult result) {
        // Vérifie si le createur est donné
        if (offer.getCreateur() == null || offer.getCreateur().getId() ==null )
            return ResponseEntity.badRequest().body("Le créateur est obligatoire");

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        //cette ligne cherche l'User dans la BD qui a cet id spécifique et le stocke en createur.
        Optional<User> createur = userRepository.findById(offer.getCreateur().getId()); // "offer.getCreateur().getId()" = prend ce qui est dans la variable/l'attribut createur en tant qu'un id

        // Vérifie que l'utilisateur existe dans la DB
        if (createur.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable");
        // Vérifie que c'est bien une entreprise
        if (createur.get().getRole() != User.Role.ENTREPRISE)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Seules les entreprises peuvent créer des offres");
        // Associe le créateur à l'offre
        offer.setCreateur(createur.get());
        // Définit la date de publication à aujourd'hui si non fournie
        if (offer.getDatePublication() == null)
            offer.setDatePublication(LocalDate.now());
        // Définit le statut par défaut
        if (offer.getStatut() == null)
            offer.setStatut(InternshipOffer.Statut.OUVERTE);
        // Sauvegarde l'offre
        InternshipOffer nouvelleOffre = offerRepository.save(offer);
        return ResponseEntity.status(HttpStatus.CREATED).body(nouvelleOffre); // 201 Created
    }

    // 2. Obtenir toutes les offres
    @GetMapping
    public ResponseEntity<List<InternshipOffer>> obtenirTousLesOffres() {
        List<InternshipOffer> offers = offerRepository.findAll();
        return ResponseEntity.ok(offers); // 200 OK
    }

    // 3. Obtenir une offre par ID -
    @GetMapping("/{id}")
    public ResponseEntity<InternshipOffer> obtenirOffreParId(@PathVariable Long id) {
        Optional<InternshipOffer> offer = offerRepository.findById(id);

        if (offer.isPresent()) {
            return ResponseEntity.ok(offer.get()); // 200 OK
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    // 4. Obtenir les offres d'une entreprise
    @GetMapping("/entreprise/{user_id}")
    public ResponseEntity<?> obtenirOffresParEntreprise(@PathVariable Long user_id) {
        Optional<User> createur = userRepository.findById(user_id);

        if (createur.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entreprise introuvable");

        // List<InternshipOffer> offers = offerRepository.findByCreateur(createur.get()); si on n'a pas fais la relation bidirectionnelle @OneToMany dans User
        List<InternshipOffer> offers = createur.get().getOffres();
        return ResponseEntity.ok(offers);

    }
    // 5. Rechercher des offres par ville
    @GetMapping("/ville/{ville}")
    public ResponseEntity<List<InternshipOffer>> obtenirOffresParVille(@PathVariable String ville) {

        List<InternshipOffer> offers = offerRepository.findByVille(ville);
        return ResponseEntity.ok(offers);
    }
    // 6. Rechercher des offres par titre
    @GetMapping("/search")
    public ResponseEntity<List<InternshipOffer>> obtenirOffresParTitre(@RequestParam String titre) {

        List<InternshipOffer> offers = offerRepository.findByTitreContainingIgnoreCase(titre);
        return ResponseEntity.ok(offers);
    }
    // 7. Filtrer par statut
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<InternshipOffer>> obtenirOffresParStatut(@PathVariable InternshipOffer.Statut statut) {

        List<InternshipOffer> offers = offerRepository.findByStatut(statut);
        return ResponseEntity.ok(offers);
    }
    // 8. Modifier une offre - PUT /api/offers/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierOffre(@PathVariable Long id, @RequestBody InternshipOffer offerDetails) {
        Optional<InternshipOffer> offerExistante = offerRepository.findById(id);

        if (offerExistante.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("L'offre n'existe pas"); // 404
        }

        InternshipOffer offer = offerExistante.get();

        // Met à jour les champs (sauf le créateur et la date de publication)
        offer.setTitre(offerDetails.getTitre());
        offer.setDescription(offerDetails.getDescription());
        offer.setEntreprise(offerDetails.getEntreprise());
        offer.setVille(offerDetails.getVille());
        offer.setDuree(offerDetails.getDuree());
        offer.setRemuneration(offerDetails.getRemuneration());
        offer.setCompetencesRequises(offerDetails.getCompetencesRequises());
        offer.setDateDebut(offerDetails.getDateDebut());
        offer.setStatut(offerDetails.getStatut());

        InternshipOffer offerMiseAJour = offerRepository.save(offer);
        return ResponseEntity.ok(offerMiseAJour); // 200 OK
    }
    // 9. Supprimer une offre - DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerOffre(@PathVariable Long id) {
        if (offerRepository.existsById(id)) {
            offerRepository.deleteById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}