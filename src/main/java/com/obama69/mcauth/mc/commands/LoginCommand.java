package com.obama69.mcauth.mc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.obama69.mcauth.MCAuth;
import com.obama69.mcauth.mc.WorldAuthSession;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class LoginCommand {

	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("login")
					.requires(s -> s.hasPermission(0))
					.then(
							Commands.argument("password", StringArgumentType.greedyString())
							.executes(LoginCommand::doLogin)
					)
		);
	}
	
	private static int doLogin(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final CommandSourceStack source = context.getSource();
		final ServerPlayer player = source.getPlayerOrException();
		final String password = StringArgumentType.getString(context, "password");
		
		final WorldAuthSession session = MCAuth.instance.getSession();
		
		session.onLoginAttempt(player, password);
		
		return 1;
	}
}
