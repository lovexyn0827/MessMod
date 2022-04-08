package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.options.InvaildOptionException;
import lovexyn0827.mess.options.Option;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.options.OptionParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class MessCfgCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("messcfg").requires(CommandUtil.COMMAND_REQUMENT)
				.executes((ct) -> {
					ModMetadata metadata = FabricLoader.getInstance().getModContainer("messmod").get().getMetadata();
					ServerCommandSource s = ct.getSource();
					s.sendFeedback(new LiteralText(metadata.getName() + " " + metadata.getVersion()).formatted(Formatting.BOLD), false);
					CommandUtil.feedback(ct, metadata.getDescription());
					s.sendFeedback(new LiteralText("Current Options").formatted(Formatting.BOLD), false);
					OptionManager.OPTIONS.forEach((f) -> {
						String n = f.getName();
						String v = OptionManager.getString(f);
						MutableText text = new LiteralText(n + ": " + v).formatted(Formatting.GRAY);
						boolean modified = !v.equals(f.getAnnotation(Option.class).defaultValue());
						s.sendFeedback(modified ? text.append(new LiteralText(" (Modified)").formatted(Formatting.BOLD, Formatting.RED)) : text, false);
					});
					return 1;
				})
				.then(literal("reloadConfig")
						.executes((ct) -> {
							OptionManager.reload();
							CommandUtil.feedback(ct, "Reloaded config");
							return Command.SINGLE_SUCCESS;
						}));
				/*.
				then(literal("railNoAutoConnection").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct)->{
									MessMod.INSTANCE.setOption("railNoAutoConnection", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
								})))*/
		OptionManager.OPTIONS.forEach((f) -> {
			try {
				Option o = f.getAnnotation(Option.class);
				OptionParser<?> parser = o.parserClass().newInstance();
				SuggestionProvider<ServerCommandSource> sp = parser.createSuggestions();
				if(sp == null) {
					sp = CommandUtil.immutableSuggestions(o.suggestions());
				}
				
				command.then(literal(f.getName()).requires(CommandUtil.COMMAND_REQUMENT)
						.executes((ct) -> {
							MutableText text = new LiteralText(f.getName()).formatted(Formatting.GREEN);
							if(o.experimental()) {
								text.append(new LiteralText("(Experimental!)").formatted(Formatting.RED));
							}
							
							text.append(new LiteralText("\n" + o.description() + "\n").formatted(Formatting.GRAY));
							String value = OptionManager.getString(f);
							text.append(new LiteralText("Current Value: " + value).formatted(Formatting.WHITE));
							if(!o.defaultValue().equals(value)) {
								text.append(new LiteralText("(Modified)").formatted(Formatting.RED));
							}
							
							text.append(new LiteralText("\nDefault Value: " + o.defaultValue()).formatted(Formatting.WHITE));
							ct.getSource().sendFeedback(text, false);
							return Command.SINGLE_SUCCESS;
						})
						.then(argument("value", StringArgumentType.greedyString())
								.suggests(sp)
								.executes((ct) -> {
									try {
										String value = StringArgumentType.getString(ct, "value");
										Object obj = parser.tryParse(value);
										OptionManager.set(f, obj);
										OptionManager.CUSTOM_APPLICATION_BEHAVIORS.computeIfPresent(f.getName(), (name, behavior) -> {
											behavior.accept(value, ct);
											return behavior;
										});
										CommandUtil.feedback(ct, "Option " + f.getName() + " is now set to " + value + ".");
										return Command.SINGLE_SUCCESS;
									} catch (InvaildOptionException e) {
										CommandUtil.error(ct, e.getMessage());
										return -1;
									}
								})));
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
		});
		dispatcher.register(command);
	}
}
