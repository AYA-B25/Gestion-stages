package com.stage.gestion_stages.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "candidatures")
@Data
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String messageMotivation;

    @Column(length = 500)
    private String cvPath;

    @Column(nullable = false)
    private LocalDate dateCandidature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("candidat-candidatures")
    private User candidat;

    @ManyToOne
    @JoinColumn(name = "offer_id", nullable = false)
    @JsonBackReference("offre-candidatures")
    private InternshipOffer offre;

    public enum Statut {
        EN_ATTENTE, ACCEPTEE, REFUSEE
    }


}
