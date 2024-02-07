package com.github.cunvoas.geoserviceisochrone.service.admin;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestPasswordEncoder {

	static private PasswordEncoder passwordEncoder1;
	static private PasswordEncoder passwordEncoder2;
	static private PasswordEncoder passwordEncoder3;

	static int saltLen=12;
	static int hashLen=30;
	static int nbThreads=1;
	static int nbIter=31;
	String samplPass="1234567890POIU%ùYTRE°09";

	@BeforeAll
	static void init() {
		
		passwordEncoder1 = new Argon2PasswordEncoder(saltLen, hashLen, nbThreads, 32768, nbIter);
		passwordEncoder2 = new Argon2PasswordEncoder(128, 128, 1, 32768, nbIter);
		passwordEncoder3 = new Argon2PasswordEncoder(128, 128, 4, 32768, nbIter);
//		passwordEncoder3 = new Argon2PasswordEncoder(128, 128, 8, 32768, nbIter);
		
	}
	
	@Test
	void test() {
		int loops=3;

		long st = System.nanoTime();
		for (int i = 0; i < loops; i++) {
			passwordEncoder1.encode(samplPass);
		}
		System.out.println((System.nanoTime()-st)/loops);
		
		
		st = System.nanoTime();
		for (int i = 0; i < loops; i++) {
			passwordEncoder2.encode(samplPass);
		}
		System.out.println((System.nanoTime()-st)/loops);
		

		st = System.nanoTime();
		for (int i = 0; i < loops; i++) {
			passwordEncoder3.encode(samplPass);
		}
		System.out.println((System.nanoTime()-st)/loops);
		

		st = System.nanoTime();
		for (int i = 0; i < loops; i++) {
			passwordEncoder2.encode(samplPass);
		}
		System.out.println((System.nanoTime()-st)/loops);
	}
}
