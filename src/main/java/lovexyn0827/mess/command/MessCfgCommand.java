package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.InvaildOptionException;
import lovexyn0827.mess.options.Option;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.options.OptionParser;
import lovexyn0827.mess.util.FormattedText;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class MessCfgCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("messcfg").requires(CommandUtil.COMMAND_REQUMENT)
				.executes((ct) -> {
					ModMetadata metadata = FabricLoader.getInstance().getModContainer("messmod").get().getMetadata();
					ServerCommandSource s = ct.getSource();
					s.sendFeedback(new LiteralText(metadata.getName() + " " + metadata.getVersion()).formatted(Formatting.BOLD), false);
					CommandUtil.feedbackRaw(ct, metadata.getDescription());
					s.sendFeedback(new FormattedText("cmd.messcfg.list", "l").asMutableText(), false);
					OptionManager.OPTIONS.forEach((f) -> {
						Option o = f.getAnnotation(Option.class);
						String n = f.getName();
						String v = OptionManager.getString(f);
						ClickEvent event = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/messcfg " + n);
						MutableText text = new LiteralText(n + ": " + v)
								.fillStyle(Style.EMPTY.withClickEvent(event)
										.withHoverEvent((new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(o.description())))))
								.formatted(Formatting.GRAY);
						boolean modified = !v.equals(f.getAnnotation(Option.class).defaultValue());
						s.sendFeedback(modified ? text.append(new FormattedText("cmd.messcfg.modified", "cl").asMutableText()) : text, false);
					});
					return 1;
				})
				.then(literal("reloadConfig")
						.executes((ct) -> {
							OptionManager.reload();
							CommandUtil.feedback(ct, "cmd.messcfg.reload");
							return Command.SINGLE_SUCCESS;
						}))
				.then(literal("reloadMapping")
						.executes((ct) -> {
							MessMod.INSTANCE.reloadMapping();
							CommandUtil.feedback(ct, "cmd.messcfg.reloadmapping");
							return Command.SINGLE_SUCCESS;
						}));
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
							MutableText text = new FormattedText(f.getName(), "a", false).asMutableText();
							if(o.experimental()) {
								text.append(new FormattedText("cmd.messcfg.exp", "rcl").asMutableText());
							}
							
							text.append(new LiteralText("\n" + o.description() + "\n").formatted(Formatting.GRAY));
							String value = OptionManager.getString(f);
							text.append(new FormattedText("cmd.messcfg.current", "f", true, value).asMutableText());
							if(!o.defaultValue().equals(value)) {
								text.append(new FormattedText("cmd.messcfg.modified", "cl").asMutableText());
							}
							
							text.append(new FormattedText("cmd.messcfg.global", "f", true, o.defaultValue()).asMutableText());
							text.append(new FormattedText("cmd.messcfg.default", "f", true, o.defaultValue()).asMutableText());
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
										CommandUtil.feedbackWithArgs(ct, "cmd.messcfg.set", f.getName(), value);
										return Command.SINGLE_SUCCESS;
									} catch (InvaildOptionException e) {
										CommandUtil.error(ct, e.getMessage());
										return -1;
									}
								})));
				command.then(literal("setGlobal").requires(CommandUtil.COMMAND_REQUMENT)
						.then(literal(f.getName()).requires(CommandUtil.COMMAND_REQUMENT)
								.then(argument("value", StringArgumentType.greedyString())
										.suggests(sp)
										.executes((ct) -> {
											try {
												String value = StringArgumentType.getString(ct, "value");
												Object obj = parser.tryParse(value);
												OptionManager.setGolbal(f, obj);
												OptionManager.CUSTOM_APPLICATION_BEHAVIORS.computeIfPresent(f.getName(), (name, behavior) -> {
													behavior.accept(value, ct);
													return behavior;
												});
												CommandUtil.feedbackWithArgs(ct, "cmd.messcfg.setglobal", f.getName(), value);
												return Command.SINGLE_SUCCESS;
											} catch (InvaildOptionException e) {
												e.printStackTrace();
												CommandUtil.error(ct, e.getMessage());
												return -1;
											}
										}))));
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
		});
		dispatcher.register(command);
	}
}
