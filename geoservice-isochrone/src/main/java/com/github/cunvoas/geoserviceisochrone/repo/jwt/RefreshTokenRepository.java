package com.github.cunvoas.geoserviceisochrone.repo.jwt;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.jwt.RefreshToken;

/**
 * Repository pour gérer les refresh tokens JWT.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Trouve un refresh token par sa valeur.
     * @param token la valeur du token
     * @return le refresh token si trouvé
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Trouve un refresh token par contributeur.
     * @param contributeur le contributeur
     * @return le refresh token si trouvé
     */
    Optional<RefreshToken> findByContributeur(Contributeur contributeur);

    /**
     * Supprime tous les tokens expirés et révoqués d'un contributeur.
     * @param contributeur le contributeur
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.contributeur = :contributeur AND (rt.revoked = true OR rt.expiryDate < CURRENT_TIMESTAMP)")
    void deleteExpiredAndRevokedByContributeur(Contributeur contributeur);

    /**
     * Supprime tous les tokens d'un contributeur.
     * @param contributeur le contributeur
     */
    void deleteByContributeur(Contributeur contributeur);
}

