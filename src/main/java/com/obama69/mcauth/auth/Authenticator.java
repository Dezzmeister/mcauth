package com.obama69.mcauth.auth;

import java.security.NoSuchAlgorithmException;

public interface Authenticator {
	
	boolean isValidLogin(String username, String password, String expected) throws NoSuchAlgorithmException;
	
	String generateStoreValue(String username, String password) throws NoSuchAlgorithmException;
}
