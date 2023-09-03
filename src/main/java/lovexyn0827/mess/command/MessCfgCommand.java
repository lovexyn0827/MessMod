package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.InvalidOptionException;
import lovexyn0827.mess.options.Option;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.options.OptionParser;
import lovexyn0827.mess.util.FormattedText;
import lovexyn0827.mess.util.i18n.I18N;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MessCfgCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("messcfg").requires(CommandUtil.COMMAND_REQUMENT)
				.executes((ct) -> {
					ModMetadata metadata = FabricLoader.getInstance().getModContainer("messmod").get().getMetadata();
					ServerCommandSource s = ct.getSource();
					s.sendFeedback(Text.literal(metadata.getName() + " " + metadata.getVersion()).formatted(Formatting.BOLD), false);
					CommandUtil.feedbackRaw(ct, metadata.getDescription());
					s.sendFeedback(new FormattedText("cmd.messcfg.list", "l").asMutableText(), false);
					OptionManager.OPTIONS.forEach((f) -> {
						String n = f.getName();
						String v = OptionManager.getString(f);
						ClickEvent event = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/messcfg " + n);
						MutableText text = Text.literal(n + ": " + v)
								.fillStyle(Style.EMPTY.withClickEvent(event)
										.withHoverEvent((new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
												Text.literal(OptionManager.getDescription(n))))))
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
			Option o = f.getAnnotation(Option.class);
			if(!OptionManager.isSupportedInCurrentEnv(o)) {
				return;
			}
			
			OptionParser<?> parser = OptionParser.of(o);
			SuggestionProvider<ServerCommandSource> sp = parser.createSuggestions();
			if(sp == null) {
				sp = CommandUtil.immutableSuggestions((Object[]) o.suggestions());
			}
			
			command.then(literal(f.getName()).requires(CommandUtil.COMMAND_REQUMENT)
					.executes((ct) -> {
						MutableText text = new FormattedText(f.getName(), "a", false).asMutableText();
						if(o.experimental()) {
							text.append(new FormattedText("cmd.messcfg.exp", "rcl").asMutableText());
						}
						
						text.append(Text.literal("\n" + OptionManager.getDescription(f.getName()) + "\n")
								.formatted(Formatting.GRAY));
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
								String value = StringArgumentType.getString(ct, "value");
								if(o.globalOnly()) {
									MutableText errMsg = Text.literal(I18N.translate("cmd.messcfg.globalonly", f.getName()))
											.fillStyle(Style.EMPTY
													.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, 
															"/messcfg setGlobal " + f.getName() + ' ' + value)));
									ct.getSource().sendError(errMsg);
									return -1;
								}
								
								try {
									Object obj = parser.tryParse(value);
									if(OptionManager.set(f, obj, ct)) {
										CommandUtil.feedbackWithArgs(ct, "cmd.messcfg.set", f.getName(), value);
										return Command.SINGLE_SUCCESS;
									}
									
									return 0;
								} catch (InvalidOptionException e) {
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
											if(OptionManager.setGlobal(f, obj)) {
												CommandUtil.feedbackWithArgs(ct, "cmd.messcfg.setglobal", f.getName(), value);
												return Command.SINGLE_SUCCESS;
											}
											
											return 0;
										} catch (InvalidOptionException e) {
											e.printStackTrace();
											CommandUtil.error(ct, e.getMessage());
											return -1;
										}
									}))));
		});
		dispatcher.register(command);
	}
}
