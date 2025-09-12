package com.github.cunvoas.geoserviceisochrone.model.tools;

import lombok.Data;

/**
 * DTO représentant un email destiné à un contributeur.
 * Contient les informations nécessaires pour l'envoi d'un message personnalisé.
 */
@Data
public class EmailToContributor {
    /**
     * Adresse email du contributeur.
     */
    private String email;
    /**
     * Nom du contributeur.
     */
    private String name;
    /**
     * Sujet de l'email.
     */
    private String subject;
    /**
     * Corps du message à envoyer.
     */
    private String message;
    /**
     * Logo à inclure dans l'email (chemin ou encodage).
     */
    private String logoAutmel;
    /**
     * Statut de l'email (ex : envoyé, en attente, erreur).
     */
    private String status;
}