package com.github.cunvoas.geoserviceisochrone.model.tools;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entité représentant une photo associée à un parc.
 * Contient les informations de stockage, de localisation et d'identification de la photo.
 */
@Data
@EqualsAndHashCode(of = {"id"})
@Entity(name = "parc_photo")
@Table(indexes = {
          @Index(name = "idx_parcphoto_loc", columnList = "communeId, location"),
          @Index(name = "idx_parcphoto_hash", columnList = "hash")
        })
public class ParkPhoto {

    /**
     * Identifiant unique de la photo (clé primaire).
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_photo")
    @SequenceGenerator(
            name="seq_photo",
            allocationSize=1,
            initialValue = 1 )
    private Long id;

    /**
     * Identifiant du parc associé à la photo.
     */
    @Column(name = "parc_id")
    private Long parcId;
    /**
     * Identifiant de la commune.
     */
    @Column(name = "commune_id")
    private Long communeId;
    /**
     * Code INSEE de la commune (5 caractères).
     */
    @Column(name = "insee_code", length = 5)
    private String inseeCode;
    /**
     * Dossier de stockage de la photo.
     */
    @Column(name = "folder", length = 255)
    private String storedFolder;
    /**
     * Nom actuel du fichier stocké.
     */
    @Column(name = "name", length = 50)
    private String currentFileName;
    /**
     * Nom d'origine du fichier.
     */
    @Column(name = "name_org", length = 50)
    private String originalFileName;
    /**
     * Hash du fichier d'origine (pour vérification d'intégrité).
     */
    @Column(name = "hash", length = 64)
    private String originalFileHash;
    /**
     * Localisation géographique de la photo (coordonnées).
     */
    @Column(name = "location")
    private Point location;
}