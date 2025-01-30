package com.github.cunvoas.geoserviceisochrone.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            String ip = request.getRemoteAddr();
        	loginAttemptService.loginFailed(ip);
            log.error("IP reported case 1 {}", ip);
        } else {
        	String ip = xfHeader.split(",")[0];
            loginAttemptService.loginFailed(xfHeader.split(",")[0]);
            log.error("IP reported case 2 {}", ip);
        }
    }
}