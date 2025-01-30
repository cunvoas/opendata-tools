package com.github.cunvoas.geoserviceisochrone.model.admin;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = {"id"})
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Entity(name = "adm_asso")
public class Association {

	@Id
	@ToString.Include
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_asso")
    @SequenceGenerator(
    		name="seq_asso",
    		allocationSize=1,
    		initialValue = 1
    	)
	private Long id;

	@ToString.Include
	private String nom;
	private String email;
	private String logo;
	private String description;

	@Column(name = "site_url")
	private String siteUrl;
	@Column(name = "hello_asso_url")
	private String helloAssoUrl;
	
	
}
