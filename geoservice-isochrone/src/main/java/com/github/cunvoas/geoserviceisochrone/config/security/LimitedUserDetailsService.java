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

/**
 *  @see https://www.baeldung.com/spring-security-block-brute-force-authentication-attempts
 */
@Service("userDetailsService")
public class LimitedUserDetailsService  implements UserDetailsService {
	
	@Value("${application.security.timehack:1000}")
	private Long timeHack=1000L;
 
	@Autowired
	private final ContributeurRepository userRepository = null;

    @Autowired
    private LoginAttemptService loginAttemptService;
 
    
    private Long getMitigationTimeHack() {
    	return timeHack+Double.doubleToLongBits(Math.random()*timeHack/7);
    }
    
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
        		// time hack fix
        		Thread.sleep(getMitigationTimeHack());
        		throw new UsernameNotFoundException("User not found");
        	}
 
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
