package com.github.cunvoas.geoserviceisochrone.service.admin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.github.cunvoas.geoserviceisochrone.model.admin.Association;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.repo.admin.AssociationRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ContributeurRepository;

@SpringBootTest
@ActiveProfiles({"prod"})
//@TestPropertySource(locations = "classpath:application-secret.yml")
class TestPasswordService {

	@Autowired
	private PasswordService passwordService;
	
	@Autowired
	private ContributeurRepository contributeurRepository;
	@Autowired
	private AssociationRepository associationRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	// stand alone test
	private PasswordEncoder tested;
	
//	@Value("${INIT_ADMINMAP_LOGIN}")
	private String initLOGIN;
//	@Value("${INIT_ADMINMAP_EMAIL}")
	private String initEmail;
//	@Value("${INIT_ADMINMAP_PWORD}")
	private String initPwd;
	
	
	@Test
	@Disabled
	void adminUser() {
		Contributeur contrib = null;
		Optional<Contributeur> opt =contributeurRepository.findByLogin("adminAutmel");
		if (opt.isPresent()) {
			contrib = opt.get();
		} else {
			contrib = new Contributeur();
			contrib.setLogin(initLOGIN);
			contrib.setPrenom("Christophe");
			contrib.setNom("Administrateur des cartes");
			contrib.setEmail(initEmail);
			contrib.setRole(ContributeurRole.ADMINISTRATOR);
			contrib.setAssociation(null);
			
		}
		contrib.setPassword(passwordEncoder.encode(initPwd));
		contributeurRepository.save(contrib);
	}
	

	
	@Test
	@Disabled
	void createUser() {
		//see: /mnt/nas_home/ASSO
	}

	@Test
	void isSafe() {
		assertFalse(passwordService.isSafe("password"), "weak1");
		assertFalse(passwordService.isSafe("12345678912345"), "weak2");
		assertFalse(passwordService.isSafe("123456789123456"), "weak3");
		assertTrue(passwordService.isSafe("Az#456789123456"), "safe1");
	}

	
	
	@Test
	@Disabled
	void test() {
		String plain  = "THIS_IS_MY_PASSWORD THIS_IS_MY_PASSWORD THIS_IS_MY_PASSWORD THIS_IS_MY_PASSWORD THIS_IS_MY_PASSWORD THIS_IS_MY_PASSWORD THIS_IS_MY_PASSWORD THIS_IS_MY_PASSWORD THIS_IS_MY_PASSWORD THIS_IS_MY_PASSWORD ";
		
		int memSizeInKb =  1 << 11;
		System.out.println( memSizeInKb);
		long mem =Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("mem start "+mem);
		tested  =  new Argon2PasswordEncoder(
						30, 60, 
						1, memSizeInKb,
						1021);
		System.out.println("Mem =     "+ String.valueOf(
				Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() -mem 
				));
		
		
		long deb = System.currentTimeMillis();
		String cypher = tested.encode(plain);
		
		Boolean matched = tested.matches(plain, cypher);
		Assertions.assertTrue(matched, "correct");


		
		
		System.out.println("Duration = "+ String.valueOf( System.currentTimeMillis()-deb) );
		System.out.println(cypher);
		System.out.println("len="+cypher.length());
		
		
		plain  = "This5$ùǜ!µé";
		cypher = tested.encode(plain);
		System.out.println(cypher);
		System.out.println("len="+cypher.length());
	}

}
