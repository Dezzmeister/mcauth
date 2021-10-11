package com.obama69.mcauth.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import com.obama69.mcauth.MCAuth;

/**
 * Authenticator using SHA512 to hash passwords. Adds a 16-byte salt to each password and prepends it to the hash.
 * 
 * @author Joe Desmond
 */
public class Authenticator512 implements Authenticator {
	private static final int SALT_BYTES = 16;

	@Override
	public boolean isValidLogin(final String username, final String password, final String expected) throws NoSuchAlgorithmException {
		final byte[] entryBytes = Base64.getDecoder().decode(expected);
		final byte[] salt = new byte[SALT_BYTES];
		
		System.arraycopy(entryBytes, 0, salt, 0, salt.length);
		
		final String testHash = computeB64Hash(password, salt);
		
		return (testHash.equals(expected));
	}

	@Override
	public String generateStoreValue(final String username, final String password) throws NoSuchAlgorithmException {
		return computeB64Hash(password, null);
	}
	
	private final String computeB64Hash(final String raw, final byte[] defaultSalt) throws NoSuchAlgorithmException {
		MessageDigest digest;
		
		try {
			digest = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			MCAuth.LOGGER.error("Unable to use SHA512, falling back to SHA256");
			
			try {
				digest = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e1) {
				e.printStackTrace();
				MCAuth.LOGGER.error("Tried to fall back to SHA256 and failed");
				e1.printStackTrace();
				
				throw e1;
			}
		}
		
		final byte[] salt;
		
		if (defaultSalt != null) {
			salt = defaultSalt;
		} else {
			salt = generateSalt();
		}
		
		final byte[] rawBytes = raw.getBytes();
		final byte[] plaintext = new byte[salt.length + rawBytes.length];
		
		System.arraycopy(salt, 0, plaintext, 0, salt.length);
		System.arraycopy(rawBytes, 0, plaintext, salt.length, rawBytes.length);
		
		final byte[] hashedPlaintext = digest.digest(plaintext);
		final byte[] fullHashed = new byte[salt.length + hashedPlaintext.length];
		
		System.arraycopy(salt, 0, fullHashed, 0, salt.length);
		System.arraycopy(hashedPlaintext, 0, fullHashed, salt.length, hashedPlaintext.length);
		
		return Base64.getEncoder().encodeToString(fullHashed);
	}
	
	private final byte[] generateSalt() {
		final byte[] out = new byte[SALT_BYTES];
		final SecureRandom random = new SecureRandom();
		
		random.nextBytes(out);
		
		return out;
	}
}
