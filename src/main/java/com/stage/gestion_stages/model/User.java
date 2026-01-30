package com.stage.gestion_stages.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "createur",cascade = CascadeType.ALL)
    @JsonManagedReference("createur-offres") //pour eviter les boucles infinies
    private List<InternshipOffer> offres;

    public enum Role {
        ETUDIANT,
        ENTREPRISE,
        ADMIN
    }

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL)
    @JsonManagedReference("candidat-candidatures")
    private List<Candidature> candidatures = new ArrayList<>();



}