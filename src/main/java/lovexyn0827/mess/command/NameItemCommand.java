package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class NameItemCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("nameitem")
				.requires(CommandUtil.COMMAND_REQUMENT)
				.then(argument("name", StringArgumentType.greedyString())
						.executes((ct) -> {
							Text name = Text.literal(StringArgumentType.getString(ct, "name"));
							if (ct.getSource().getEntity() instanceof LivingEntity) {
								LivingEntity mob = ((LivingEntity) ct.getSource().getEntity());
								if (mob.getMainHandStack() != null) {
									mob.getMainHandStack().set(DataComponentTypes.CUSTOM_NAME, name);
								}
							}
							
							CommandUtil.feedback(ct, "cmd.general.success");
							return Command.SINGLE_SUCCESS;
						}));
		dispatcher.register(command);
	}
}
