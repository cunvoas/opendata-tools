package com.github.cunvoas.geoserviceisochrone.service.admin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * Business Service impl.
 * @author cunvoas
 * @see https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html
 */
@Service
public class PasswordService {
	
	@Autowired
	private final PasswordEncoder passwordEncoder=null;
	
	/**
	 * Encode the password.
	 * @param plain text
	 * @return cypher
	 */
	public String securizePassword(String plain) {
	    return passwordEncoder.encode(plain);
	}
	
	// https://uibakery.io/regex-library/password
	// https://mkyong.com/regular-expressions/how-to-validate-password-with-regular-expression/
	private static final String CHECK_REGEX= "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[-!%µ@#&_()–[{}]:;\\\"'`,?/*~$^+=<>]).{12,}$";
	/**
	 * check isSafe.
	 * @param newPass new pwd
	 * @return Boolean
	 */
	public Boolean isSafe(String newPass) {
		Boolean ret = Boolean.FALSE;
		if (newPass.matches(CHECK_REGEX)) {
			ret = Boolean.TRUE;
			
			//TODO MoSCoW: Could call service about RockYou pass exists in.
			
		}
		return ret;
	}

    /**
     * generatePassword.
     * @param length requested
     * @return new passwd
     */
    public String generatePassword(int length) {
    	if (length<12) {
    		length= 12;
    	}
        String pwString = generateRandomSpecialCharacters(3)
                .concat(generateRandomNumbers(3))
                .concat(generateRandomAlphabet(3, true))
                .concat(generateRandomAlphabet(length-9, false));

        List<Character> pwChars = pwString.chars()
                .mapToObj(data -> (char) data)
                .collect(Collectors.toList());
        Collections.shuffle(pwChars);
        String password = pwChars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();

        // dirty quick fix
        int nb=10;
        while (!isSafe(password) && nb>0) {
            password = generatePassword(length);
            nb--;
        }
        return password;
    }
    
    /**
     * generateCharactersInRange.
     * @param length requested
     * @param rangeStart asscii start
     * @param rangeEnd asscii stop
     * @return chars
     */
    protected String generateCharactersInRange(int length,int rangeStart, int rangeEnd) {
        RandomStringGenerator pwdGenerator = new RandomStringGenerator.Builder().withinRange(rangeStart, rangeEnd)
                .build();
        return pwdGenerator.generate(length);
    }
    
    /**
     * generateRandomSpecialCharacters
     * @param length requested
     * @return chars
     */
    protected String generateRandomSpecialCharacters(int length) {
        return generateCharactersInRange(length, 33, 47);
    }
    /**
     * generateRandomNumbers
     * @param length requested
     * @return chars
     */
    protected String generateRandomNumbers(int length) {
        return generateCharactersInRange(length, 48, 57);
    }
    /**
     * generateRandomAlphabet.
     * @param length requested
     * @param upperCase upper or not
     * @return chars
     */
    protected String generateRandomAlphabet(int length, boolean upperCase) {
        return generateCharactersInRange(length, upperCase ? 65 : 90, upperCase ? 90 : 122);
    }

}
