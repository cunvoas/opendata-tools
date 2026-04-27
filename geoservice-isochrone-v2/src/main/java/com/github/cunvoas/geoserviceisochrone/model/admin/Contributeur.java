package com.github.cunvoas.geoserviceisochrone.model.admin;


import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Représente un contributeur (utilisateur) de la plateforme.
 * Implémente UserDetails pour l'intégration avec Spring Security.
 */
@Data
@EqualsAndHashCode(of = {"id"})
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Entity(name = "adm_contrib")
public class Contributeur implements UserDetails {

	private static final long serialVersionUID = -7295077909019064322L;

	/**
	 * Identifiant unique du contributeur.
	 */
	@Id
	@ToString.Include
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_contrib")
    @SequenceGenerator(
    		name="seq_contrib",
    		allocationSize=1,
    		initialValue = 1
    	)
	private Long id;

	/**
	 * Nom du contributeur.
	 */
	@ToString.Include
	@Column(length = 50)
	private String nom;

	/**
	 * Prénom du contributeur.
	 */
	@Column(length = 50)
	private String prenom;
	
	/**
	 * Identifiant de connexion (login).
	 */
	@Column(length = 30)
	private String login;
	
	/**
	 * Adresse email du contributeur.
	 */
	@Column(length = 100)
	private String email;
	
	/**
	 * Mot de passe (haché) du contributeur.
	 */
	@Column(length = 500)
	private String password;

	/**
	 * URL de l'avatar du contributeur.
	 */
	@Column(length = 100)
	private String avatar;

	/**
	 * Rôle du contributeur.
	 */
	@Column(length = 30)
	private ContributeurRole role;

	/**
	 * Date de création du compte.
	 */
	@Column
	private Date creationDate;
	
	/**
	 * Date de dernière mise à jour du compte.
	 */
	@Column
	private Date updateDate;
	
	/**
	 * Date du dernier login.
	 */
	@Column
	private Date lastLoginDate;

    /**
     * Association liée au contributeur (optionnelle).
     */
    @ManyToOne
    @JoinColumn(name="asso_id", nullable=true)
    private Association association;
    
    // Préférences géographiques
    /**
     * Identifiant de la région préférée.
     */
    @Column
    private Long idRegion;
    /**
     * Identifiant de la communauté de communes préférée.
     */
    @Column
    private Long idCommunauteCommune;
    /**
     * Identifiant de la commune préférée.
     */
    @Column
    private Long idCommune;

    /**
     * Retourne les autorités (rôles) du contributeur.
     * @return collection des rôles
     */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Arrays.asList(role);
	}

	/**
	 * Retourne le login du contributeur.
	 * @return login
	 */
	@Override
	public String getUsername() {
		return login;
	}

    /**
     * Indique si le compte est expiré.
     * @return toujours true (jamais expiré)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

	@Override
	public boolean isEnabled() {
		return password!=null && password.length()>12;
	}
	
	public String getFullName() {
		return prenom+" "+nom;
	}
	
	
}