package com.obama69.mcauth.mc.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RegisterCommandsEventListener {

	@SubscribeEvent
	public void registerCommands(final RegisterCommandsEvent event) {
		final CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		
		LoginCommand.register(dispatcher);
		CreateAccountCommand.register(dispatcher);
		ChangePasswordCommand.register(dispatcher);
		ClearAccountCommand.register(dispatcher);
	}
}
