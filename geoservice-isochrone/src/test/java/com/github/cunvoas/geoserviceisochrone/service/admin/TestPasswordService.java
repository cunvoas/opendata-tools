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
@ActiveProfiles({"dev"})
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
	
	@Value("${INIT_ADMINMAP_LOGIN}")
	private String initLOGIN;
//	@Value("${INIT_ADMINMAP_EMAIL}")
	private String initEmail="";
	@Value("${INIT_ADMINMAP_PWORD}")
	private String initPwd;
	
	
	@Test
	@Disabled
	void adminUser() {
		Contributeur contrib = null;
		Optional<Contributeur> opt =contributeurRepository.findByLogin(initLOGIN);
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
	@Disabled
	void isSafe() {
		assertFalse(passwordService.isSafe("password"), "weak1");
		assertFalse(passwordService.isSafe("12345678912345"), "weak2");
		assertFalse(passwordService.isSafe("123456789123456"), "weak3");
		assertTrue(passwordService.isSafe("Az#456789123456"), "safe1");
	}
	
	@Test
	void testRegexCtrlChars() {
		assertTrue(passwordService.isSafe("Az#456789123456!"), "33");
		assertTrue(passwordService.isSafe("Az#456789123456\""), "34");
		assertTrue(passwordService.isSafe("Az#456789123456#"), "35");
		assertTrue(passwordService.isSafe("Az#456789123456$"), "36");
		assertTrue(passwordService.isSafe("Az#456789123456%"), "37");
		assertTrue(passwordService.isSafe("Az#456789123456&"), "38");
		assertTrue(passwordService.isSafe("Az#456789123456'"), "39");
		assertTrue(passwordService.isSafe("Az#456789123456("), "40");
		assertTrue(passwordService.isSafe("Az#456789123456)"), "41");
		assertTrue(passwordService.isSafe("Az#456789123456*"), "42");
		assertTrue(passwordService.isSafe("Az#456789123456+"), "43");
		assertTrue(passwordService.isSafe("Az#456789123456,"), "44");
		assertTrue(passwordService.isSafe("Az#456789123456-"), "45");
		assertTrue(passwordService.isSafe("Az#456789123456."), "46");
		assertTrue(passwordService.isSafe("Az#456789123456/"), "47");
		assertTrue(passwordService.isSafe("Az#45678912345%F-"), "%f-");
	}

	@Test
	void testRegexCtrlCharsMass() {
		for (int i = 0; i < 1000;i++) {
			String p = passwordService.generatePassword(12);
			if (passwordService.isSafe(p)) {
				assertTrue(passwordService.isSafe(p), "safe" + i);
			} else {
				System.out.println(p);
			}

		}

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
