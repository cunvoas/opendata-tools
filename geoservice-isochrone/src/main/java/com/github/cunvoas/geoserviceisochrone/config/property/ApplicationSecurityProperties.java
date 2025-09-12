package com.github.cunvoas.geoserviceisochrone.config.property;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

/**
 * Propriétés de configuration de sécurité de l’application.
 * Gère les paramètres liés à l’authentification, au chiffrement et à la sécurité des mots de passe.
 */
@Getter
@Setter
@Configuration
@Primary
@ConfigurationProperties(prefix = "application.security")
@Validated
public class ApplicationSecurityProperties {

  @NotNull
  private String authorizationHeaderName = "Authorization";

  @NotNull
  private String authorizationHeaderValue;
  
  @NotNull
  private String  jwtSignKey;
  
  // change custom values to raises entropy
  @NotNull
  private Integer saltLen=10;

  @NotNull
  private Integer hashLen=20;

  @NotNull
  private Integer nbThreads=1;
  
  @NotNull
  private Integer nbIters=2;
	
  // large memory size limits GPU brute forcing
  @NotNull
  private Integer memSizeInKb =  1024;//1 << 15; // 2^15 = 32 Mo
  
  
}