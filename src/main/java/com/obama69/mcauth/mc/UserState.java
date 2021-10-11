package com.obama69.mcauth.mc;

import java.util.Objects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class UserState {
	public final ServerPlayer player;
	public final Vec3 initialPosition;
	public final String username;
	public int loginTries;
	
	public UserState(final ServerPlayer _player, final String _username) {
		player = _player;
		initialPosition = player.position();
		username = _username;
		loginTries = 0;
	}
	
	public void onFailedLoginAttempt() {
		loginTries++;
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof UserState)) {
			return false;
		}
		
		final UserState otherState = (UserState) other;
		return username.equals(otherState.username);
	}
	
	public int hashCode() {
		return Objects.hash(username);
	}
}
