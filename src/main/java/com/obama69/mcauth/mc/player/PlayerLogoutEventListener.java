package com.obama69.mcauth.mc.player;

import com.obama69.mcauth.MCAuth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerLogoutEventListener {
	
	@SubscribeEvent
	public void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
		final Player player = event.getPlayer();
		
		if (!(player instanceof ServerPlayer)) {
			return;
		}
		
		final ServerPlayer serverPlayer = (ServerPlayer) player;
		
		MCAuth.instance.getSession().onPlayerDisconnected(serverPlayer);
	}
}
