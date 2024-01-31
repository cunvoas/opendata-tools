package com.github.cunvoas.geoserviceisochrone.config.security.unused;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationSecurityProperties;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cus
 * static header for reverse-proxy check.
 */
@Slf4j
//@Component
public class TokenAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

  private final ApplicationSecurityProperties customProps;

  public TokenAuthenticationFilter(ApplicationSecurityProperties customProps) {
    super();
    this.customProps = customProps;

    this.setAuthenticationManager(authentication -> {
      String credentials = (String) authentication.getCredentials();

      if (customProps.getAuthorizationHeaderValue().equals(credentials)) {
        authentication.setAuthenticated(true);
        return authentication;
      }

      throw new BadCredentialsException(String.format("has invalid header '%s'", customProps.getAuthorizationHeaderName()));
    });
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    return "api-key-auth";
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return request.getHeader(customProps.getAuthorizationHeaderName());
  }

  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
      throws IOException, ServletException {
    super.unsuccessfulAuthentication(request, response, failed);

    log.error("Not authenticated request [{} {}] {}", request.getMethod(), request.getServletPath(), failed.getMessage());
  }
}
