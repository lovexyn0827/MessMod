package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.ArgumentListTokenizer;
import lovexyn0827.mess.util.FormattedText;
import lovexyn0827.mess.util.MethodDescriptor;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.access.AccessingFailureException;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import lovexyn0827.mess.util.access.InvalidLiteralException;
import lovexyn0827.mess.util.access.Literal;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class VariableCommand {
	private static final Pattern CONSTRUCTOR_FORMAT = Pattern.compile("^(?<class>[0-9a-zA-Z_$/\\.]+)"
			+ "(<(?:(?<argNum>[0-9]+)|(?<desc>\\((?:\\[*(?:L[a-zA-Z0-9/]+;)|I|D|S|J|Z|F|C|B)*\\)V))>)?$");
	private static final LinkedHashMap<String, Object> VARIABLES = new LinkedHashMap<>();
	private static final ImmutableMap<String, Function<CommandContext<ServerCommandSource>, Object>>
			BUILTIN_OBJECT_PROVIDERS = Util.make(() -> {
				ImmutableMap.Builder<String, Function<CommandContext<ServerCommandSource>, Object>> b = 
						ImmutableMap.builder();
				b.put("server", (ct) -> ct.getSource().getServer());
				b.put("sender", CommandContext::getSource);
				b.put("world", (ct) -> ct.getSource().getWorld());
				b.put("senderEntity", (ct) -> ct.getSource().getEntity());
				if(!MessMod.isDedicatedServerEnv()) {
					b.put("client", (ct) -> MinecraftClient.getInstance());
					b.put("clientWorld", (ct) -> MinecraftClient.getInstance().world);
					b.put("clientPlayer", (ct) -> MinecraftClient.getInstance().player);
				}
				
				return b.build();
			});
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> slotSuggestion = (ct, b) -> {
			VARIABLES.keySet().forEach(b::suggest);
			return b.buildFuture();
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("variable")
				.requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("set")
						.then(argument("slot", StringArgumentType.word())
								.suggests(slotSuggestion)
								.then(literal("new")
										.then(argument("constructor", StringArgumentType.string())
												.suggests(CommandUtil.immutableSuggestions(
														"\"net/minecraft/", "java/lang/", "java/util/"))
												.executes((ct) -> setNewObject(ct, false))
												.then(argument("args", StringArgumentType.greedyString())
														.executes((ct) -> setNewObject(ct, true)))))
								.then(literal("literal")
										.then(argument("value",StringArgumentType.greedyString())
												.executes(VariableCommand::setLiteral)))
								.then(literal("entity")
										.then(argument("selector", EntityArgumentType.entity())
												.executes(VariableCommand::setEntity)))
								.then(argument("objSrc", StringArgumentType.word())
										.suggests(CommandUtil.immutableSuggestions(
												BUILTIN_OBJECT_PROVIDERS.keySet().toArray()))
										.executes(VariableCommand::setBulitin))))
				.then(literal("map")
						.then(argument("slotSrc", StringArgumentType.word())
								.suggests(slotSuggestion)
								.then(argument("slotDst", StringArgumentType.word())
										.suggests(slotSuggestion)
										.then(argument("func", AccessingPathArgumentType.accessingPathArg())
												.executes(VariableCommand::map)))))
				.then(literal("print")
						.then(argument("slot", StringArgumentType.word())
								.suggests(slotSuggestion)
								.then(literal("array")
										.executes((ct) -> {
											String slot = StringArgumentType.getString(ct, "slot");
											Object ob = VARIABLES.get(slot);
											if(!ob.getClass().isArray()) {
												CommandUtil.error(ct, "cmd.variable.reqarray");
												return 0;
											}
											
											CommandUtil.feedbackRaw(ct, 
													Arrays.toString((Object[]) ob));
											return Command.SINGLE_SUCCESS;
										}))
								.then(literal("toString")
										.executes((ct) -> {
											String slot = StringArgumentType.getString(ct, "slot");
											CommandUtil.feedbackRaw(ct, VARIABLES.get(slot));
											return Command.SINGLE_SUCCESS;
										}))
								.then(literal("dumpFields")
										.executes((ct) -> {
											String slot = StringArgumentType.getString(ct, "slot");
											ct.getSource().sendFeedback(toText(VARIABLES.get(slot)), false);
											return Command.SINGLE_SUCCESS;
										}))))
				.then(literal("list")
						.executes((ct) -> {
							VARIABLES.forEach((key, val) -> {
								CommandUtil.feedbackRaw(ct, String.format("%s = %.48s", 
										key, val == null ? "null" : val.toString()));
							});
							return Command.SINGLE_SUCCESS;
						}));
		dispatcher.register(command);
	}
	
	private static Text toText(Object ob) {
		Mapping map = MessMod.INSTANCE.getMapping();
		MutableText text = new FormattedText(map.namedClass(ob.getClass().getName()), "").asMutableText();
		text.append(new FormattedText("[", "rla").asMutableText());
		Iterator<Field> itr = Reflection.getInstanceFields(ob.getClass()).iterator();
		while(itr.hasNext()) {
			Field f = itr.next();
			f.setAccessible(true);
			text.append(new FormattedText(map.namedField(f.getName()), "rc").asMutableText());
			text.append(new FormattedText(map.namedField("="), "r6").asMutableText());
			try {
				Object val = f.get(ob);
				text.append(new FormattedText(val == null ? "null" : val.toString(), "r7").asMutableText());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				text.append(new FormattedText(f.getName(), "r7k").asMutableText());
			}
			
			if(itr.hasNext()) {
				text.append(new FormattedText(map.namedField(", "), "rl5").asMutableText());
			}
		}
		
		text.append(new FormattedText("]", "rla").asMutableText());
		return text;
	}

	private static int setNewObject(CommandContext<ServerCommandSource> ct, boolean hasArgs) 
			throws CommandSyntaxException {
		String slot = StringArgumentType.getString(ct, "slot");
		String constructorStr = StringArgumentType.getString(ct, "constructor");
		Mapping map = MessMod.INSTANCE.getMapping();
		Matcher m = CONSTRUCTOR_FORMAT.matcher(constructorStr);
		if(m.matches()) {
			String className = m.group("class").replace('/', '.');
			Class<?> cl;
			try {
				cl = Class.forName(map.srgClass(className));
			} catch (ClassNotFoundException e) {
				CommandUtil.errorWithArgs(ct, "exp.noclass", className);
				return 0;
			}
			
			Constructor<?>[] constructors = cl.getDeclaredConstructors();
			Set<Constructor<?>> targets = new HashSet<>();
			if(m.group("argNum") != null) {
				int argNum = Integer.parseInt(m.group("argNum"));
				for(Constructor<?> c : constructors) {
					if(c.getParameterCount() == argNum) {
						targets.add(c);
					}
				}
			} else if(m.group("desc") != null) {
				MethodDescriptor desc = MethodDescriptor.parse(m.group("desc"));
				for(Constructor<?> c : constructors) {
					if(Arrays.equals(c.getParameterTypes(), desc.argTypes)) {
						targets.add(c);
					}
				}
			} else if(constructors.length == 1) {
				targets.add(constructors[0]);
			} else {
				CommandUtil.error(ct, "exp.multitarget");
				return 0;
			}
			
			if(targets.size() == 0) {
				CommandUtil.errorWithArgs(ct, "exp.noconstructor", className);
				return 0;
			} else if (targets.size() == 1) {
				Constructor<?> c = targets.iterator().next();
				Object[] args;
				if(hasArgs) {
					String argsStr = StringArgumentType.getString(ct, "args");
					ArgumentListTokenizer tokenizer = new ArgumentListTokenizer(argsStr);
					try {
						String[] argsStrArray = tokenizer.toArray();
						if(argsStrArray.length != c.getParameterCount()) {
							CommandUtil.errorWithArgs(ct, "exp.badarg", argsStr, c);
							return 0;
						}
						
						args = new Object[argsStrArray.length];
						for(int i = 0; i < argsStrArray.length; i++) {
							if (!argsStrArray[i].isEmpty()) {
								try {
									args[i] = Literal.parse(argsStrArray[i]);
								} catch (CommandSyntaxException e) {
									throw e;
								}
							} else {
								throw new TranslatableException("exp.emptyarg");
							}
						}
					} catch (TranslatableException e) {
						CommandUtil.errorRaw(ct, e.getLocalizedMessage(), e);
						return 0;
					}
				} else {
					args = new Object[0];
				}
				
				c.setAccessible(true);
				Object newObj;
				try {
					newObj = c.newInstance(args);
				} catch (IllegalAccessException | InstantiationException e) {
					CommandUtil.errorWithArgs(ct, "exp.unexc", e);
					e.printStackTrace();
					return 0;
				} catch (IllegalArgumentException e) {
					CommandUtil.errorWithArgs(ct, "exp.badarg", Arrays.toString(args), c);
					return 0;
				} catch (InvocationTargetException e) {
					CommandUtil.errorWithArgs(ct, "exp.failexec", c, e);
					e.getCause().printStackTrace();
					return 0;
				}
				
				VARIABLES.put(slot, newObj);
			} else {
				CommandUtil.error(ct, "exp.multitarget");
				return 0;
			}
		} else {
			CommandUtil.error(ct, "cmd.variable.invconstructor");
			return 0;
		}

		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}
	
	private static int setLiteral(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		String slot = StringArgumentType.getString(ct, "slot");
		String valStr = StringArgumentType.getString(ct, "value");
		Object val;
		try {
			val = Literal.parse(valStr).get(null);
		} catch (CommandSyntaxException e) {
			throw e;
		} catch (InvalidLiteralException e) {
			CommandUtil.errorRaw(ct, e.getLocalizedMessage(), e);
			return 0;
		}
		
		VARIABLES.put(slot, val);
		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}
	
	private static int setEntity(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		String slot = StringArgumentType.getString(ct, "slot");
		Entity e = EntityArgumentType.getEntity(ct, "selector");
		VARIABLES.put(slot, e);
		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}
	
	private static int setBulitin(CommandContext<ServerCommandSource> ct) {
		String slot = StringArgumentType.getString(ct, "slot");
		String name = StringArgumentType.getString(ct, "objSrc");
		Function<CommandContext<ServerCommandSource>, Object> getter = BUILTIN_OBJECT_PROVIDERS.get(name);
		if(getter != null) {
			VARIABLES.put(slot, getter.apply(ct));
		} else {
			CommandUtil.errorWithArgs(ct, "cmd.general.nodef", name);
			return 0;
		}

		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}
	
	private static int map(CommandContext<ServerCommandSource> ct) {
		String slotS = StringArgumentType.getString(ct, "slotSrc");
		String slotD = StringArgumentType.getString(ct, "slotDst");
		AccessingPath path = AccessingPathArgumentType.getAccessingPath(ct, "func");
		if(VARIABLES.containsKey(slotS)) {
			Object objSrc = VARIABLES.get(slotS);
			try {
				VARIABLES.put(slotD, path.access(objSrc, objSrc.getClass()));
			} catch (AccessingFailureException e) {
				CommandUtil.errorRaw(ct, e.getLocalizedMessage(), e);
				return 0;
			}
		} else {
			CommandUtil.errorWithArgs(ct, "cmd.general.nodef", slotS);
			return 0;
		}

		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}
	
	public static void reset() {
		VARIABLES.clear();
	}

	public static @Nullable Object getVariable(String slot) {
		return VARIABLES.get(slot);
	}
}
