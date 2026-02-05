package com.stage.gestion_stages.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity // Dit à Spring : "Cette classe = une table dans MySQL"
@Table(name = "internship_offers") // Le nom de la table sera "internship_offers"
@Data // Génère automatiquement les getters, setters, toString(), equals(), hashCode()
public class InternshipOffer {

    @Id // Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrémentation
    private Long id;

    @NotBlank(message = "Le titre est obligatoire") // Au lieu de validé dans le Controller. NotBlank = pas null ET pas vide
    @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
    @Column(nullable = false, length = 200) // NOT NULL, maximum 200 caractères
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Column(nullable = false, columnDefinition = "TEXT") // NOT NULL, texte long
    private String description;

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Column(nullable = false, length = 150)
    private String entreprise;

    @NotBlank(message = "La ville est obligatoire")
    @Column(nullable = false, length = 100)
    private String ville;

    @NotNull(message = "La durée est obligatoire")
    @Column(nullable = false) // Durée en mois
    private Integer duree;

    @Column // Peut être NULL (rémunération optionnelle)
    private Double remuneration;

    @Column(columnDefinition = "TEXT") // Texte long, peut être NULL
    private String competencesRequises;

    @NotNull(message = "La date de début est obligatoire")
    @Column(nullable = false)
    private LocalDate dateDebut; // Date de début du stage

    @Column(nullable = false)
    private LocalDate datePublication; // Date de publication de l'offre

    @Enumerated(EnumType.STRING) // Stocke en texte : "OUVERTE", "FERMEE", "POURVUE"
    @Column(nullable = false)
    private Statut statut;

    // Relation MANY-TO-ONE : Plusieurs offres → Une entreprise
    // Une offre appartient à UN utilisateur (l'entreprise qui l'a publiée)
    @ManyToOne // Plusieurs offres peuvent être créées par un seul utilisateur
    @JoinColumn(name = "user_id", nullable = false) // Colonne "user_id" dans la table, NOT NULL
    @JsonBackReference("createur-offres")
    private User createur; // L'entreprise qui a créé l'offre

    // Enum pour le statut de l'offre
    public enum Statut {
        OUVERTE,   // L'offre est active, on peut postuler
        FERMEE,    // L'offre est fermée, on ne peut plus postuler
        POURVUE    // Le stage a été attribué à un candidat
    }

    @OneToMany(mappedBy = "offre",cascade = CascadeType.ALL)
    @JsonManagedReference("offre-candidatures")
    private List<Candidature> candidatures = new ArrayList<>();

}