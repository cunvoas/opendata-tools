package com.github.cunvoas.geoserviceisochrone.config.security.unused;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationSecurityProperties;

import jakarta.servlet.http.HttpServletRequest;

//@Component("localNetworkAuthenticationFilter")
public class LocalNetworkAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

	private static final Pattern PATTERN = Pattern.compile(
			"(^127\\.0\\.0\\.1)|(^10\\.)|(^172\\.1[6-9]\\.)|(^172\\.2[0-9]\\.)|(^172\\.3[0-1]\\.)|(^192\\.168\\.)");

	public LocalNetworkAuthenticationFilter(ApplicationSecurityProperties customProps) {
		super();

		this.setAuthenticationManager(authentication -> {
			Boolean isLocal = (Boolean) authentication.getCredentials();

			if (isLocal) {
				authentication.setAuthenticated(true);
				return authentication;
			}

			throw new BadCredentialsException("not a LAN address");
		});
	}

	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		return "local-network";
	}

	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		String address = request.getRemoteAddr();
		final String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader != null && !xfHeader.isEmpty()) {
			address = xfHeader.split(",")[0];
		}

		Matcher matcher = PATTERN.matcher(address);
		return matcher.matches();
	}
	
	

}
