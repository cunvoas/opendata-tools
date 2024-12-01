package com.github.cunvoas.geoserviceisochrone.config.security.unused;

import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.authentication.AuthenticationManagerBeanDefinitionParser.NullAuthenticationProvider;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring security config.<br>
 * Spring docs :
 * <ul>
 * <li><a href=
 * "https://docs.spring.io/spring-security/site/docs/3.2.0.CI-SNAPSHOT/reference/html/headers.html">spring-security-headers.html</a></li>
 * <li><a href=
 * "https://www.baeldung.com/spring-boot-security-autoconfiguration">spring-boot-security-autoconfiguration</a></li>
 * </ul>
 *
 * Headers best practice :
 * <ul>
 * <li><a href="https://www.w3.org/TR/referrer-policy/">referrer-policy</a></li>
 * <li><a href="https://www.w3.org/TR/feature-policy/">feature-policy</a></li>
 * <li><a href=
 * "https://www.veracode.com/blog/2014/03/guidelines-for-setting-security-headers">guidelines-for-setting-security-headers</a></li>
 * </ul>
 *
 * Audit Headers :
 * <ul>
 * <li><a href="https://securityheaders.com/">securityheaders.com</a></li>
 * </ul>
 *
 * Multi config by endpoint:
 * <ul>
 * <li><a href=
 * "https://stackoverflow.com/questions/35890540/when-to-use-spring-securitys-antmatcher">when-to-use-spring-securitys-antmatcher</a></li>
 * </ul>
 */
//@Configuration
@Slf4j
//@EnableWebSecurity
public class WebSecurityConfiguration {

//	  @Order(0)
//	  @Bean
//	  public SecurityFilterChain unsecureFilter(HttpSecurity http) throws Exception {
//	    return this.httpSecurityDefaultConfig(http)
//	        .securityMatcher(
//	            "/login",
//	            "/error",
//	            "/favicon.ico")
//
//	        .authorizeHttpRequests(authorizeRequestsCustomizer -> authorizeRequestsCustomizer
//	            .requestMatchers(AnyRequestMatcher.INSTANCE)
//	            .permitAll())
//
//	        .build();
//	  }
//  @Order(1)
//  @Bean
//  public SecurityFilterChain unsecureFilter(HttpSecurity http) throws Exception {
//    return this.httpSecurityDefaultConfig(http)
//        .securityMatcher(
//            "/awake",
//          	"/health",
//          	"/info",
//          	"/prometheus",
//            "/actuator/prometheus",
//            "/login",
//            "/error",
//            "/favicon.ico")
//
//        .authorizeHttpRequests(authorizeRequestsCustomizer -> authorizeRequestsCustomizer
//            .requestMatchers(AnyRequestMatcher.INSTANCE)
//            .permitAll())
//
//        .build();
//  }

//  @Order(2)
//  @Bean
//  public SecurityFilterChain secureFilter(HttpSecurity http, TokenAuthenticationFilter tokenAuthenticationFilter) throws Exception {
//    return this.httpSecurityDefaultConfig(http)
//        .securityMatcher("/**")
//
//        .addFilter(tokenAuthenticationFilter)
//
//        .authorizeHttpRequests(authorizeRequestsCustomizer -> authorizeRequestsCustomizer
//            .requestMatchers(AnyRequestMatcher.INSTANCE)
//            .authenticated())
//
//        .exceptionHandling()
//        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
//        .and()
//
//        .build();
//  }

  @SuppressWarnings("")
  private HttpSecurity httpSecurityDefaultConfig(HttpSecurity http) throws Exception {
    return http
        .httpBasic().disable()

       // .csrf().disable()

        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()

        .formLogin().disable()

        // OWASP Hardening header.
        .headers()
        .defaultsDisabled()
        .addHeaderWriter(new StaticHeadersWriter("Referrer-Policy", "origin"))
        .addHeaderWriter(new StaticHeadersWriter("Feature-Policy", "unsized-media 'none'; geolocation 'none'"))

        .cacheControl()
        .and()

        .contentTypeOptions()
        .and()

        .xssProtection()
        .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
        .and()

        .frameOptions().sameOrigin()

        .httpStrictTransportSecurity()
        .disable()

        .contentSecurityPolicy("default-src 'self'")
        
        //fix
        .and()
        .and();
  }

  /**
   * Evict to have {@link InMemoryUserDetailsManager} provided by {@link UserDetailsServiceAutoConfiguration}
   */
  @Bean
  public NullAuthenticationProvider nullAuthenticationProvider() {
    return new NullAuthenticationProvider();
  }

}
