package com.obama69.mcauth.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.obama69.mcauth.MCAuth;
import com.obama69.mcauth.auth.Authenticator;

/**
 * Authentication DB. Persisted in a tab-delimited file in the server directory.
 * 
 * @author Joe Desmond
 */
public class AuthStore {
	private static final String DB_FILENAME = "authdb.txt";
	
	/**
	 * Maps usernames to password hashes
	 */
	private final Map<String, String> users;
	
	/**
	 * Authentication method
	 */
	private final Authenticator authenticator;
	
	public AuthStore(final Authenticator _authenticator) {
		users = loadStore();
		authenticator = _authenticator;
	}
	
	public boolean userExists(final String username) {
		return users.containsKey(username);
	}
	
	public void removeUser(final String username) {
		if (!userExists(username)) {
			throw new IllegalArgumentException("User \"" + username + "\" does not have an account");
		}
		
		users.remove(username);
		persist();
	}
	
	public boolean login(final String username, final String password) throws NoSuchAlgorithmException {
		if (!userExists(username)) {
			throw new IllegalArgumentException("User \"" + username + "\" does not have an account");
		}
		
		final String storedHash = users.get(username);
		
		return authenticator.isValidLogin(username, password, storedHash);
	}
	
	public void createUser(final String username, final String password) throws NoSuchAlgorithmException {
		if (userExists(username)) {
			throw new IllegalArgumentException("User \"" + username + "\" already exists");
		}
		
		users.put(username, authenticator.generateStoreValue(username, password));
		persist();
	}
	
	public void changePassword(final String username, final String newPassword) throws NoSuchAlgorithmException {
		if (!userExists(username)) {
			throw new IllegalArgumentException("User \"" + username + "\" does not have an account");
		}
		
		users.put(username, authenticator.generateStoreValue(username, newPassword));
		persist();
	}
	
	public void persist() {
		final List<String> lines = new ArrayList<String>();
		
		for (final Entry<String, String> entry : users.entrySet()) {
			lines.add(entry.getKey() + "\t" + entry.getValue());
		}
		
		try {
			MCAuth.LOGGER.info("Peristing Auth DB");
			Files.write(Paths.get(DB_FILENAME), lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Map<String, String> loadStore() {
		final File potentialStore = new File(DB_FILENAME);
		
		if (potentialStore.exists()) {
			return loadStoreFromFile(DB_FILENAME);
		}
		
		return new HashMap<String, String>();
	}
	
	private static Map<String, String> loadStoreFromFile(final String path) {
		final HashMap<String, String> out = new HashMap<String, String>();
		
		try {
			final List<String> lines = Files.readAllLines(Paths.get(path));
			
			for (final String line : lines) {
				final String[] parts = line.split("\t");
				
				if (parts.length != 2) {
					MCAuth.LOGGER.error("Error reading authdb.txt: \"" + line + "\"");
					continue;
				}
				
				out.put(parts[0], parts[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return out;
	}
}
