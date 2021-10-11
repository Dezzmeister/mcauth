package com.obama69.mcauth.mc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.obama69.mcauth.MCAuth;
import com.obama69.mcauth.mc.WorldAuthSession;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ClearAccountCommand {

	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("clearaccount")
					.requires(s -> s.hasPermission(4))
					.then(
							Commands.argument("username", StringArgumentType.string())
							.executes(ClearAccountCommand::doClearAccount)
					)
		);
	}
	
	private static int doClearAccount(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final CommandSourceStack source = context.getSource();
		final String username = StringArgumentType.getString(context, "username");
		
		final WorldAuthSession session = MCAuth.instance.getSession();
		
		session.tryRemoveUser(source, username);
		
		return 1;
	}
}
