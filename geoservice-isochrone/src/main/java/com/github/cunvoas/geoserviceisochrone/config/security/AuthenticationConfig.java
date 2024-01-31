package com.github.cunvoas.geoserviceisochrone.config.security;

import static org.springframework.security.config.Customizer.withDefaults;

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
 * @author cus
 * @see https://security.stackexchange.com/questions/247936/since-gpus-have-gigabytes-of-memory-does-argon2id-need-to-use-gigabytes-of-memo
 * @see https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Slf4j
public class AuthenticationConfig {
	
	private final ApplicationSecurityProperties customProps;
	public AuthenticationConfig(ApplicationSecurityProperties customProps) {
		super();
		this.customProps = customProps;
	}
	
	
	@Autowired
	private LimitedUserDetailsService userDetailsService;
	
	@Bean 
	public RequestContextListener requestContextListener(){
	    return new RequestContextListener();
	} 
		
//	@Bean
//	@Order(10)                                                        
//	public SecurityFilterChain monitorFilterChain(HttpSecurity http) throws Exception {
//		return http
//				.securityMatcher(
//		            "/awake",
//		          	"/health",
//		          	"/info",
//		          	"/prometheus",
//		            "/actuator/prometheus"
//		          )                             
//				.authorizeHttpRequests(localNetworkAuthenticationFilter -> localNetworkAuthenticationFilter
//					.anyRequest()
//					.authenticated()
//				)
//				.formLogin(withDefaults())
//			.build();
//	}

	
	
	@Bean
	@Order(2)                                                        
	public SecurityFilterChain geoFilterChain(HttpSecurity http) throws Exception {
		return http
				.securityMatcher("/map/**")                                   
				.authorizeHttpRequests(authorize -> authorize
						.anyRequest()
						.permitAll()
					)
			.build();
	}
	
	
	@Bean
	@Order(1)                                                        
	public SecurityFilterChain mvcliFilterChain(HttpSecurity http) throws Exception {
		return http
				.securityMatcher("/mvc/**")                                   
				.authorizeHttpRequests(authorize -> authorize
					.anyRequest()
					.authenticated()
				)
				.formLogin(withDefaults())
			.build();
	}
	
	@Bean       
	@Order(0)                                                        
	public SecurityFilterChain freeFilterChain(HttpSecurity http) throws Exception {
		return
			http
				.securityMatcher(
			        "/awake",
		            "/login",
		            "/logout",
		            "/favicon.ico")                             
				.authorizeHttpRequests(authorize -> authorize
					.anyRequest()
					.permitAll()
				)
				.formLogin(withDefaults())
			.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		Integer saltLen = customProps.getSaltLen();
		if (saltLen == 10) {
			log.warn("please custom application.security.passwordSaltLen");
		}
		if (customProps.getHashLen() == 20) {
			log.warn("please custom application.security.passwordHashLen");
		}
		if (customProps.getNbIters() <= 10) {
			log.warn("please custom application.security.passwordNbIters");
		}
		if (customProps.getMemSizeInKb() <= 1 << 10) {
			log.warn("please custom application.security.passwordMemSizeInKb");
		}
		return new Argon2PasswordEncoder(customProps.getSaltLen(), customProps.getHashLen(), 1,
				customProps.getMemSizeInKb(), customProps.getNbIters());

	}
	
	
}
