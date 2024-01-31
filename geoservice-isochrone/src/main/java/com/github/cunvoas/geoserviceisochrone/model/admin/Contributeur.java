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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "adm_contrib")
public class Contributeur implements UserDetails {

	private static final long serialVersionUID = -7295077909019064322L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_contrib")
	private Long id;

	@Column(length = 50)
	private String nom;
	@Column(length = 50)
	private String prenom;
	
	@Column(length = 30)
	private String login;
	
	@Column(length = 100)
	private String email;
	
	@Column(length = 200)
	private String password;

	@Column(length = 100)
	private String avatar;

	@Column(length = 30)
	private ContributeurRole role;

	@Column
	private Date creationDate;
	
	@Column
	private Date updateDate;

    @ManyToOne
    @JoinColumn(name="asso_id", nullable=true)
    private Association association;

    
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Arrays.asList(role);
	}

	@Override
	public String getUsername() {
		return login;
	}


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
