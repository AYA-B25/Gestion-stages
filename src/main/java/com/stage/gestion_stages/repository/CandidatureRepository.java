package com.stage.gestion_stages.repository;

import com.stage.gestion_stages.model.Candidature;
import com.stage.gestion_stages.model.InternshipOffer;
import com.stage.gestion_stages.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    List<Candidature> findApplicationsByCandidat(User candidat);

    List<Candidature> findApplicationsByOffre (InternshipOffer offre);

    List<Candidature> findApplicationsByStatut (Candidature.Statut statut);

    Optional<Candidature> findByCandidatAndOffre(User candidat, InternshipOffer offre);

}
