package com.github.cunvoas.geoserviceisochrone.controller.mvc.validator;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
/**
 * Classe responsable de la gestion des tokens de sécurité.
 * @author cunvoas
 */
@Component
@Slf4j
public class TokenManagement {
	
	@Value("${application.security.token.expiration-ms}")
	private long tokenExpirationMillis;
	
	@Value("${application.security.token.secret}")
	private String tokenSecret;
	
	public String getValidToken() {
		String now = String.valueOf(System.currentTimeMillis());
		
		byte[]  tokenSecretBytes = Base64.getDecoder().decode(tokenSecret);
		SecretKey key = new SecretKeySpec(tokenSecretBytes, "ChaCha20");
		
		try {
			// Generate a random nonce for each encryption
			byte[] nonce = getNonce();
			byte[] encrypted = encrypt(now.getBytes(), key, nonce);
			
			// Combine nonce and encrypted data (nonce is needed for decryption)
			byte[] combined = new byte[nonce.length + encrypted.length];
			System.arraycopy(nonce, 0, combined, 0, nonce.length);
			System.arraycopy(encrypted, 0, combined, nonce.length, encrypted.length);
			
			return Base64.getEncoder().encodeToString(combined);
			
		} catch (InvalidKeyException e) {
			log.error("InvalidKeyException during token encryption", e);
		} catch (NoSuchPaddingException e) {
			log.error("NoSuchPaddingException during token encryption", e);
		} catch (NoSuchAlgorithmException e) {
			log.error("NoSuchAlgorithmException during token encryption", e);
		} catch (InvalidAlgorithmParameterException e) {
			log.error("InvalidAlgorithmParameterException during token encryption", e);
		} catch (BadPaddingException e) {
			log.error("BadPaddingException during token encryption", e);
		} catch (IllegalBlockSizeException e) {
			log.error("IllegalBlockSizeException during token encryption", e);
		}
		return now;
	}
	
	public boolean isTokenValid(String token) {
		boolean valid=false;
		
		try {
			byte[]  tokenSecretBytes = Base64.getDecoder().decode(tokenSecret);
			
			byte[] combined = Base64.getDecoder().decode(token);
			
			// Extract nonce and encrypted data
			if (combined.length < 12) {
				log.error("Invalid token: too short");
				return false;
			}
			
			byte[] nonce = new byte[12];
			byte[] encryptedData = new byte[combined.length - 12];
			System.arraycopy(combined, 0, nonce, 0, 12);
			System.arraycopy(combined, 12, encryptedData, 0, encryptedData.length);
			
			SecretKey key = new SecretKeySpec(tokenSecretBytes, "ChaCha20");
			String decrypted=new String(decrypt(encryptedData, key, nonce));
			long tokenTime = Long.parseLong(decrypted);
			
			log.info("Token time: {}, now: {}", tokenTime, System.currentTimeMillis());

			valid = System.currentTimeMillis()  < tokenTime + tokenExpirationMillis;
			
		} catch (Exception e) {
			log.error("Exception during token decryption", e);
		}
		
		return valid;
	}
	

    // 96-bit nonce (12 bytes)
    private static byte[] getNonce() {
        byte[] newNonce = new byte[12];
        new SecureRandom().nextBytes(newNonce);
        return newNonce;
    }
    
	
 // A 256-bit secret key (32 bytes)
    private static SecretKey getKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("ChaCha20");
        keyGen.init(256, SecureRandom.getInstanceStrong());
        return keyGen.generateKey();
    }
    
    public String getStringKey() throws NoSuchAlgorithmException {
    	return Base64.getEncoder().encodeToString( getKey().getEncoded() );
    }
	

    private static byte[] encrypt(byte[] data, SecretKey key, byte[] nonce) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if(key == null) throw new InvalidKeyException("SecretKey must NOT be NULL");
        if(nonce == null || nonce.length != 12) throw new InvalidAlgorithmParameterException("Nonce must be 12 bytes");

        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");

        // Create IvParamterSpec
        AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonce);

        // Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "ChaCha20");

        // Initialize Cipher for ENCRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

        // Perform Encryption
        return cipher.doFinal(data);
    }

    private static byte[] decrypt(byte[] cipherText, SecretKey key, byte[] nonce) throws Exception {
        if(key == null) throw new InvalidKeyException("SecretKey must NOT be NULL");
        if(nonce == null || nonce.length != 12) throw new InvalidAlgorithmParameterException("Nonce must be 12 bytes");

        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");

        // Create IvParamterSpec
        AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonce);

        // Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "ChaCha20");

        // Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);

        // Perform Decryption
        return cipher.doFinal(cipherText);
    }

	
}
