package com.github.cunvoas.geoserviceisochrone.config.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ContributeurRepository;

/**
 *  @see https://www.baeldung.com/spring-security-block-brute-force-authentication-attempts
 */
@Service("userDetailsService")
public class LimitedUserDetailsService  implements UserDetailsService {
 
	@Autowired
	private final ContributeurRepository userRepository = null;

    @Autowired
    private LoginAttemptService loginAttemptService;
 
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (loginAttemptService.isBlocked()) {
            throw new RuntimeException("blocked");
        }
 
        try {
        	Optional<Contributeur> opUser = userRepository.findByLogin(username);
        	if (opUser.isPresent()) {
        		return opUser.get();
        	} else {
        		throw new UsernameNotFoundException("User not found");
        	}
 
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
