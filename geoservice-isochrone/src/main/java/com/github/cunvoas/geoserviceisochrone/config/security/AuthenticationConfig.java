package com.github.cunvoas.geoserviceisochrone.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.request.RequestContextListener;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationSecurityProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration de l'authentification des utilisateurs.
 * <p>
 * Configure la sécurité, les filtres, le provider d'authentification et le gestionnaire d'authentification.
 * </p>
 * @author cunvoas
 * @see https://security.stackexchange.com/questions/247936/since-gpus-have-gigabytes-of-memory-does-argon2id-need-to-use-gigabytes-of-memo
 * @see https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Slf4j
public class AuthenticationConfig {
	
	private final ApplicationSecurityProperties customProps;
	/**
	 * Constructeur avec injection des propriétés de sécurité personnalisées.
	 * @param customProps propriétés de sécurité personnalisées
	 */
	public AuthenticationConfig(ApplicationSecurityProperties customProps) {
		super();
		this.customProps = customProps;
	}
	
	@Autowired
	private LimitedUserDetailsService userDetailsService;
	
	/**
	 * Fournit un listener de contexte de requête HTTP.
	 * @return le listener de contexte de requête
	 */
	@Bean 
	public RequestContextListener requestContextListener(){
	    return new RequestContextListener();
	} 


	/**
	 * Chaîne de filtres de sécurité pour les endpoints publics et authentifiés.
	 * @param http configuration HTTP
	 * @return la chaîne de filtres de sécurité
	 * @throws Exception en cas d'erreur de configuration
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain mvcFilterChain1(HttpSecurity http) throws Exception {
		return http

				 .authorizeHttpRequests(authorizeRequests ->
		                 authorizeRequests
		                         .requestMatchers(
//			                 			    "/awake",
//			               		            "/favicon.ico";
			             			    "/actuator/**",
		                        		"/pub/**",
		                        		"/map/**").permitAll()
		                         .anyRequest().authenticated()
		         )
				.formLogin(form->form
						.loginPage("/login")
						.permitAll()
				)
				.logout(logout->logout
						.logoutSuccessUrl("/logout?m=logout")
						.permitAll()
				)
				
//			   .rememberMe(Customizer.withDefaults())
				
				.csrf(csrf->csrf
						.ignoringRequestMatchers(
								"/actuator/**",
								"/pub/**",
		                		"/map/**",
                 			    "/awake",
                 			    "/favicon.ico"
						)
				)
				
			.build();
	}

	/**
	 * Fournit le provider d'authentification basé sur les utilisateurs et le mot de passe.
	 * @return le provider d'authentification
	 */
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	/**
	 * Fournit le gestionnaire d'authentification.
	 * @param config configuration d'authentification
	 * @return le gestionnaire d'authentification
	 * @throws Exception en cas d'erreur
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	/**
	 * Fournit l'encodeur de mots de passe Argon2.
	 * @return encodeur de mots de passe
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		Integer saltLen = customProps.getSaltLen();
		// check and log WARN if default values
		if (saltLen == 10) {
			log.warn("please custom application.security.passwordSaltLen");
		} else if (saltLen == 10) {
			log.error("too small application.security.passwordSaltLen");
		}
		
		if (customProps.getHashLen() == 20) {
			log.warn("please custom application.security.passwordHashLen");
		} else if (customProps.getHashLen() < 64) {
			log.error("too small application.security.passwordHashLen");
		}
		
		if (customProps.getNbIters() <= 10) {
			log.warn("please custom application.security.passwordNbIters");
		}
		if (customProps.getMemSizeInKb() <= 1 << 10) {
			log.warn("please custom application.security.passwordMemSizeInKb");
		}
		return new Argon2PasswordEncoder(
					customProps.getSaltLen(), customProps.getHashLen(), 
					customProps.getNbThreads(), customProps.getMemSizeInKb(), customProps.getNbIters() );

	}
		
	
}