package com.github.cunvoas.geoserviceisochrone.controller.mvc.validator;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Gestion des tokens anti-rejeu par signature HMAC-SHA256.
 *
 * Le token est un timestamp signé par HMAC-SHA256 en base64.
 * Pas d'encryption : l'integrite seule suffit pour l'antirejeu.
 * HMAC-SHA256 est rapide (pas d'acceleration hardware necessaire).
 *
 * Format du token : base64( timestamp "." HMAC-SHA256(timestamp) )
 *
 * @author cunvoas
 */
@Component
@Slf4j
public class TokenManagement {

	@Value("${application.security.token.expiration-ms}")
	private long tokenExpirationMillis;

	@Value("${application.security.token.secret}")
	private String tokenSecret;

	/**
	 * Genere un token valide pour la soumission de formulaire.
	 * Le timestamp est signe par HMAC-SHA256 avec la clef secrete.
	 * L'horodatage permet de borner la duree de validite.
	 *
	 * @return token en base64 (timestamp "." hmac)
	 */
	public String getValidToken() {
		String now = String.valueOf(System.currentTimeMillis());
		try {
			String hmac = hmac(now);
			return Base64.getEncoder().encodeToString((now + "." + hmac).getBytes());
		} catch (Exception e) {
			log.error("HMAC error", e);
			return now;
		}
	}

	/**
	 * Valide un token anti-rejeu.
	 * Verifie : integrite HMAC + expiration (tokenExpirationMillis).
	 *
	 * @param token token a valider
	 * @return true si le token est valide et non expire
	 */
	public boolean isTokenValid(String token) {
		try {
			String decoded = new String(Base64.getDecoder().decode(token));
			String[] parts = decoded.split("\\.", 2);
			if (parts.length != 2) {
				return false;
			}
			String timestamp = parts[0];
			String hmac = parts[1];

			String expected = hmac(timestamp);
			if (!hmac.equals(expected)) {
				log.warn("Invalid HMAC");
				return false;
			}

			long tokenTime = Long.parseLong(timestamp);
			return System.currentTimeMillis() < tokenTime + tokenExpirationMillis;

		} catch (Exception e) {
			log.error("Token validation error", e);
			return false;
		}
	}

	/**
	 * Calcule HMAC-SHA256 des donnees avec la clef secrete configuree.
	 */
	private String hmac(String data) throws NoSuchAlgorithmException, InvalidKeyException {
		byte[] keyBytes = Base64.getDecoder().decode(tokenSecret);
		SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(key);
		return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
	}

	/**
	 * Genere une nouvelle clef HMAC-SHA256 (256-bit).
	 */
	private static SecretKey getKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
		keyGen.init(256);
		return keyGen.generateKey();
	}

	/**
	 * Retourne une clef HMAC-SHA256 en base64.
	 * Utilitaire pour generer la valeur de {@code application.security.token.secret}.
	 *
	 * @return clef encodee en base64
	 */
	public String getStringKey() throws NoSuchAlgorithmException {
		return Base64.getEncoder().encodeToString(getKey().getEncoded());
	}

}
