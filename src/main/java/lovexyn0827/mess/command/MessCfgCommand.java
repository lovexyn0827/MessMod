package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.MessModMixinPlugin;
import lovexyn0827.mess.options.InvalidOptionException;
import lovexyn0827.mess.options.Label;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.options.OptionWrapper;
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
		Command<ServerCommandSource> listAllCmd = (ct) -> {
			ct.getSource().sendMessage(new FormattedText("cmd.messcfg.list", "l").asMutableText());
			OptionManager.OPTIONS.forEach((name, opt) -> {
				dumpOption(ct.getSource(), name, opt);
			});
			return Command.SINGLE_SUCCESS;
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("messcfg").requires(CommandUtil.COMMAND_REQUMENT)
				.executes((ct) -> {
					ModMetadata metadata = FabricLoader.getInstance().getModContainer("messmod").get().getMetadata();
					ServerCommandSource s = ct.getSource();
					s.sendMessage(Text.literal(metadata.getName() + " " + metadata.getVersion()).formatted(Formatting.BOLD));
					CommandUtil.feedbackRaw(ct, metadata.getDescription());
					s.sendMessage(new FormattedText("cmd.messcfg.labels", "l").asMutableText());
					MutableText labels = Text.literal("").formatted(Formatting.GREEN, Formatting.BOLD);
					for (Label label : Label.values()) {
						MutableText labelText = Text.literal(String.format("[%s]", label.getReadableName()));
						labelText.styled((style) -> {
							ClickEvent onClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
									String.format("/messcfg list %s", label.name()));
							HoverEvent onHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
									Text.literal(label.getDescription()));
							return style.withClickEvent(onClick).withHoverEvent(onHover).withUnderline(true);
						});
						labels.append(Text.literal(" ").append(labelText));
					}
					
					s.sendMessage(labels);
					return Command.SINGLE_SUCCESS;
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
						}))
				.then(literal("list")
						.executes(listAllCmd)
						.then(argument("label", StringArgumentType.word())
								.suggests(CommandUtil.immutableSuggestionsOfEnum(Label.class))
								.executes((ct) -> {
									Label label;
									String lName = StringArgumentType.getString(ct, "label");
									try {
										label = Label.valueOf(lName);
									} catch (IllegalArgumentException e) {
										CommandUtil.errorWithArgs(ct, "cmd.general.nodef", lName);
										return 0;
									}
									
									CommandUtil.feedbackWithArgs(ct, "cmd.messcfg.withtag", 
											lName, label.getReadableName());
									ServerCommandSource s = ct.getSource();
									OptionManager.OPTIONS.forEach((name, opt) -> {
										for(Label l0 : opt.labels()) {
											if(l0 == label) {
												dumpOption(s, name, opt);
												break;
											}
										}
									});
									return Command.SINGLE_SUCCESS;
								})));
		OptionManager.OPTIONS.forEach((name, opt) -> {
			SuggestionProvider<ServerCommandSource> sp = opt.getSuggestions();
			command.then(literal(name).requires(CommandUtil.COMMAND_REQUMENT)
					.executes((ct) -> {
						MutableText text = new FormattedText(name, "a", false).asMutableText();
						if (opt.isExperimental()) {
							text.append(new FormattedText("cmd.messcfg.exp", "rcl").asMutableText());
						}
						
						if (opt.isDeprecated()) {
							text.append(new FormattedText("cmd.messcfg.deprecated", "rcl").asMutableText());
						}
						
						text.append(Text.literal("\n" + opt.getDescription() + "\n")
								.formatted(Formatting.GRAY));
						String value = OptionManager.getActiveOptionSet().getSerialized(name);
						text.append(new FormattedText("cmd.messcfg.current", "f", true, value).asMutableText());
						if (!opt.getDefaultValue().equals(value)) {
							text.append(new FormattedText("cmd.messcfg.modified", "cl").asMutableText());
						}
						
						String globalValue = OptionManager.getGlobalOptionSet().getSerialized(name);
						text.append(new FormattedText("cmd.messcfg.global", "f", true, globalValue).asMutableText());
						if (!opt.getDefaultValue().equals(globalValue)) {
							text.append(new FormattedText("cmd.messcfg.modified", "cl").asMutableText());
						}
						
						text.append(new FormattedText("cmd.messcfg.default", "f", true, opt.getDefaultValue()).asMutableText());
						ct.getSource().sendFeedback(() -> text, false);
						return Command.SINGLE_SUCCESS;
					})
					.then(argument("value", StringArgumentType.greedyString())
							.suggests(sp)
							.executes((ct) -> {
								if(checkMixins(ct, name)) {
									String value = StringArgumentType.getString(ct, "value");
									if(opt.globalOnly()) {
										MutableText errMsg = Text.literal(I18N.translate("cmd.messcfg.globalonly", name))
												.fillStyle(Style.EMPTY
														.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, 
																"/messcfg setGlobal " + name + ' ' + value)));
										ct.getSource().sendError(errMsg);
										return -1;
									}
									
									try {
										OptionManager.getActiveOptionSet().set(name, value, ct);
										CommandUtil.feedbackWithArgs(ct, "cmd.messcfg.set", name, value);
										return Command.SINGLE_SUCCESS;
									} catch (InvalidOptionException e) {
										CommandUtil.error(ct, e.getMessage());
										return -1;
									}
								} else {
									return 0;
								}
							})));
			command.then(literal("setGlobal").requires(CommandUtil.COMMAND_REQUMENT)
					.then(literal(name).requires(CommandUtil.COMMAND_REQUMENT)
							.then(argument("value", StringArgumentType.greedyString())
									.suggests(sp)
									.executes((ct) -> {
										if(checkMixins(ct, name)) {
											try {
												String value = StringArgumentType.getString(ct, "value");
												OptionManager.getGlobalOptionSet().set(name, value, ct);
												CommandUtil.feedbackWithArgs(ct, "cmd.messcfg.setglobal", name, value);
												return Command.SINGLE_SUCCESS;
											} catch (InvalidOptionException e) {
												e.printStackTrace();
												CommandUtil.error(ct, e.getMessage());
												return -1;
											}
										} else {
											return 0;
										}
									}))));
		});
		dispatcher.register(command);
	}
	
	private static void dumpOption(ServerCommandSource source, String name, OptionWrapper opt) {
		String v = OptionManager.getActiveOptionSet().getSerialized(name);
		ClickEvent event = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/messcfg " + name);
		MutableText text = Text.literal(name + ": " + v)
				.fillStyle(Style.EMPTY.withClickEvent(event)
						.withHoverEvent((new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
								Text.literal(opt.getDescription())))))
				.formatted(Formatting.GRAY);
		boolean modified = !v.equals(opt.getDefaultValue());
		source.sendFeedback(() -> {
			return modified ? text.append(new FormattedText("cmd.messcfg.modified", "cl").asMutableText()) : text;
		}, false);
	}
	
	private static boolean checkMixins(CommandContext<ServerCommandSource> ct, String name) {
		if(MessModMixinPlugin.isFeatureAvailable(name)) {
			return true;
		} else {
			for(String mixin : MessModMixinPlugin.getAbsentMixins(name)) {
				CommandUtil.errorWithArgs(ct, "cmd.general.reqmixin", name, mixin);
			}
			
			return false;
		}
	}
}
