package com.obama69.mcauth.mc;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.obama69.mcauth.MCAuth;
import com.obama69.mcauth.auth.Authenticator512;
import com.obama69.mcauth.files.AuthStore;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Manages authentication during a server session. Users are unauthenticated when they join; they need to create an account or log in
 * to be able to play.
 * 
 * @author Joe Desmond
 */
public class WorldAuthSession {
	private final int maxLoginTries;
	private final List<String> authenticatedUsers;
	private final Map<String, UserState> unauthenticatedUsers;
	
	private final AuthStore authStore;
	
	public WorldAuthSession(final int _maxLoginTries) {
		maxLoginTries = _maxLoginTries;
		authenticatedUsers = new ArrayList<String>();
		unauthenticatedUsers = new HashMap<String, UserState>();
		
		authStore = new AuthStore(new Authenticator512());
	}
	
	public boolean isAuthenticated(final Player player) {
		final String username = getUsername(player);
		return authenticatedUsers.contains(username);
	}
	
	public void reset(final Player player) {
		final String username = getUsername(player);
		final UserState userState = unauthenticatedUsers.get(username);
		
		if (userState == null) {
			return;
		}
		
		player.moveTo(userState.initialPosition);
	}
	
	public void urgeToAuthenticate(final Player player) {
		final String username = getUsername(player);
		final String message;
		
		if (authStore.userExists(username)) {
			message = "You need to log in, do /login";
		} else {
			message = "You need to set a password, do /createaccount";
		}
		
		final MutableComponent urgentMessage = new TextComponent(message).withStyle(ChatFormatting.DARK_GRAY);
		player.sendMessage(urgentMessage, player.getUUID());
	}
	
	public void tryResetPassword(final ServerPlayer player, final String newPassword) {
		final String username = getUsername(player);
		
		if (unauthenticatedUsers.containsKey(username)) {
			final MutableComponent message = new TextComponent("Need to log in first").withStyle(ChatFormatting.DARK_RED);
			player.sendMessage(message, player.getUUID());
		} else if (authenticatedUsers.contains(username)) {
			try {
				authStore.changePassword(username, newPassword);
				
				final MutableComponent message = new TextComponent("Successfully changed password").withStyle(ChatFormatting.GREEN);
				player.sendMessage(message, player.getUUID());
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				MCAuth.LOGGER.error("MC Auth cannot function without SHA512 or SHA256. Please uninstall the plugin or install a native implementation of at least one of these algorithms.");
				System.exit(-1);
			}
		}
	}
	
	public void tryRemoveUser(final CommandSourceStack executor, final String username) {
		try {
			authStore.removeUser(username);
			
			final MutableComponent message = new TextComponent("Successfully removed \"" + username + "\"");
			executor.sendSuccess(message, false);
		} catch (IllegalArgumentException e) {
			final MutableComponent message = new TextComponent("User does not have an account").withStyle(ChatFormatting.DARK_RED);
			executor.sendFailure(message);
		}
	}
	
	public void onPlayerConnected(final ServerPlayer player) {
		final String username = getUsername(player);
		final UserState userState = new UserState(player, username);
		
		unauthenticatedUsers.put(username, userState);
	}
	
	private void kickPlayer(final ServerPlayer player, final String reason) {
		player.connection.disconnect(new TextComponent(reason));
	}
	
	public boolean onLoginAttempt(final ServerPlayer player, final String password) {
		final String username = getUsername(player);
		
		if (authenticatedUsers.contains(username)) {
			final MutableComponent alreadyLoggedInMessage = new TextComponent("Already logged in").withStyle(ChatFormatting.YELLOW);
			player.sendMessage(alreadyLoggedInMessage, player.getUUID());
			return true;
		}
		
		try {
			final boolean wasSuccessful = authStore.login(username, password);
			
			if (wasSuccessful) {
				unauthenticatedUsers.remove(username);
				authenticatedUsers.add(username);
				
				final MutableComponent goodLoginMessage = new TextComponent("Successfully logged in").withStyle(ChatFormatting.GREEN);
				player.sendMessage(goodLoginMessage, player.getUUID());
				return true;
			}
			
			
			final UserState userState = unauthenticatedUsers.get(username);
			userState.onFailedLoginAttempt();
			
			if (userState.loginTries == maxLoginTries) {
				kickPlayer(player, "Too many failed login attempts (" + maxLoginTries + ")");
				unauthenticatedUsers.remove(username);
				return false;
			}
			
			final MutableComponent badPasswordMessage = new TextComponent("Bad password (max " + maxLoginTries + " attempts)").withStyle(ChatFormatting.DARK_RED);
			player.sendMessage(badPasswordMessage, player.getUUID());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			MCAuth.LOGGER.error("MC Auth cannot function without SHA512 or SHA256. Please uninstall the plugin or install a native implementation of at least one of these algorithms.");
			System.exit(-1);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			
			final MutableComponent createAccountMessage = new TextComponent("Before doing /login, you need to create a password with /createaccount").withStyle(ChatFormatting.YELLOW);
			player.sendMessage(createAccountMessage, player.getUUID());
		}
		
		return false;
	}
	
	public void onCreateAccount(final ServerPlayer player, final String password) {
		final String username = getUsername(player);
		
		if (authStore.userExists(username)) {
			final MutableComponent accountExistsMessage = new TextComponent("Account already exists, try /login").withStyle(ChatFormatting.DARK_RED);
			player.sendMessage(accountExistsMessage, player.getUUID());
			return;
		}
		
		try {
			authStore.createUser(username, password);
			
			unauthenticatedUsers.remove(username);
			authenticatedUsers.add(username);
			
			final MutableComponent goodLoginMessage = new TextComponent("Successfully created account").withStyle(ChatFormatting.GREEN);
			player.sendMessage(goodLoginMessage, player.getUUID());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			MCAuth.LOGGER.error("MC Auth cannot function without SHA512 or SHA256. Please uninstall the plugin or install a native implementation of at least one of these algorithms.");
			System.exit(-1);
		}
	}
	
	public void onPlayerDisconnected(final ServerPlayer player) {
		final String username = getUsername(player);
		
		authenticatedUsers.remove(username);
		unauthenticatedUsers.remove(username);
	}
	
	private static final String getUsername(final Player player) {
		return player.getGameProfile().getName();
	}
}
