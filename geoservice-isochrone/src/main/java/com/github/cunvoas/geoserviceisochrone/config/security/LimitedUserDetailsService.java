package com.github.cunvoas.geoserviceisochrone.config.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ContributeurRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * login service.
 *  timehack
 *  blocked check
 * @see https://www.baeldung.com/spring-security-block-brute-force-authentication-attempts
 */
@Service("userDetailsService")
@Slf4j
public class LimitedUserDetailsService  implements UserDetailsService {
	
	@Value("${application.security.timehack:1000}")
	private Long timeHack=1000L;
 
	@Autowired
	private final ContributeurRepository userRepository = null;

    @Autowired
    private LoginAttemptService loginAttemptService;
 
    
    private void mitigateTimeHack() {
    	Double d = Math.random()*timeHack/7;
    	Long time = timeHack+d.longValue();
    	try {
    		log.warn("mitigateTimeHack: {} ms", time);
			Thread.sleep(time);
		} catch (InterruptedException ignore) {
		}
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (loginAttemptService.isBlocked()) {
        	this.mitigateTimeHack();
        	throw new UsernameNotFoundException("blocked");
        }
 
        try {
        	Optional<Contributeur> opUser = userRepository.findByLogin(username);
        	if (opUser.isPresent()) {
        		return opUser.get();
        	} else {
        		// time hack fix
        		this.mitigateTimeHack();
        		throw new UsernameNotFoundException("User not found");
        	}
 
        } catch (UsernameNotFoundException e) {
            throw e;
        }
    }

}
