package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lovexyn0827.mess.util.FloatPredicate;
import lovexyn0827.mess.util.FormattedText;
import lovexyn0827.mess.util.NameFilter;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.WrappedPath;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class LogDeathCommand {
	public static final Map<String, DeathInfoLoggingItem> SUBSCRIPTED_DEATH_PREDICATES = Maps.newHashMap();
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> predNameSuggestion = (ct, b) -> {
			SUBSCRIPTED_DEATH_PREDICATES.keySet().forEach((n) -> b.suggest("\"" + n + "\""));
			b.suggest("\"*\"");
			return b.buildFuture();
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("logdeath").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("sub")
						.then(argument("target", StringArgumentType.greedyString())
								.suggests((ct, b) -> {
									DeathInfoLoggingItem.buildSuggestions(b.getRemaining(), b);
									return b.buildFuture();
								})
								.executes((ct) -> {
									String in = StringArgumentType.getString(ct, "target").replace(" ", "");
									SUBSCRIPTED_DEATH_PREDICATES.put(in, DeathInfoLoggingItem.parse(in));
									CommandUtil.feedback(ct, "cmd.general.success");
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("unsub")
						.then(argument("target", StringArgumentType.greedyString())
								.suggests(predNameSuggestion)
								.suggests((ct, b) -> {
									SUBSCRIPTED_DEATH_PREDICATES.keySet().forEach(b::suggest);
									return b.buildFuture();
								})
								.executes((ct) -> {
									String in = StringArgumentType.getString(ct, "target").replace(" ", "");
									NameFilter filter = NameFilter.compile(in);
									int count = 0;
									Iterator<String> keys = SUBSCRIPTED_DEATH_PREDICATES.keySet().iterator();
									while (keys.hasNext()) {
										String cur = keys.next();
										if (filter.test(cur)) {
											keys.remove();
											count++;
										}
									}
									
									CommandUtil.feedbackWithArgs(ct, "cmd.general.unsub", count, count);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("subVictimField")
						.then(argument("target", StringArgumentType.string())
								.suggests(predNameSuggestion)
								.then(argument("name", StringArgumentType.word())
										.then(argument("path", AccessingPathArgumentType.accessingPathArg((ct) -> {
											return Entity.class;
										}))
												.executes((ct) -> {
													return subField(ct, (item) -> item.victimDetails);
												})))))
				.then(literal("subDamageField")
						.then(argument("target", StringArgumentType.string())
								.suggests(predNameSuggestion)
								.then(argument("name", StringArgumentType.word())
										.then(argument("path", AccessingPathArgumentType.accessingPathArg((ct) -> {
											return DamageSource.class;
										}))
												.executes((ct) -> {
													return subField(ct, (item) -> item.damageDetails);
												})))))
				.then(literal("unsubVictimField")
						.then(argument("target", StringArgumentType.string())
								.suggests(predNameSuggestion)
								.then(argument("name", StringArgumentType.word())
										.executes((ct) -> {
											return unsubField(ct, (item) -> item.victimDetails);
										}))))
				.then(literal("unsubDamageField")
						.then(argument("target", StringArgumentType.string())
								.suggests(predNameSuggestion)
								.then(argument("name", StringArgumentType.word())
										.executes((ct) -> {
											return unsubField(ct, (item) -> item.damageDetails);
										}))))
				.then(literal("setVisible")
						.then(argument("target", StringArgumentType.string())
								.suggests(predNameSuggestion)
								.then(argument("visible", BoolArgumentType.bool())
										.executes((ct) -> {
											String in = StringArgumentType.getString(ct, "target").replace(" ", "");
											NameFilter filter = NameFilter.compile(in);
											boolean visible = BoolArgumentType.getBool(ct, "visible");
											SUBSCRIPTED_DEATH_PREDICATES.forEach((k, v) -> {
												if (filter.test(k)) {
													v.setVisible(visible);
												}
											});
											return Command.SINGLE_SUCCESS;
										}))))
				.then(literal("getStats")
						.executes((ct) -> {
							SUBSCRIPTED_DEATH_PREDICATES.forEach((k, v) -> {
								CommandUtil.feedbackRawWithArgs(ct, "%s: %d", k, v.getTriggerCount());
							});
							return Command.SINGLE_SUCCESS;
						})
						.then(argument("target", StringArgumentType.string())
								.suggests(predNameSuggestion)
								.executes((ct) -> {
									String in = StringArgumentType.getString(ct, "target").replace(" ", "");
									NameFilter filter = NameFilter.compile(in);
									SUBSCRIPTED_DEATH_PREDICATES.forEach((k, v) -> {
										if (filter.test(k)) {
											CommandUtil.feedbackRawWithArgs(ct, "%s: %d", k, v.getTriggerCount());
										}
									});
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}
	
	static void reset() {
		SUBSCRIPTED_DEATH_PREDICATES.clear();
	}
	
	private static int subField(CommandContext<ServerCommandSource> ct, 
			Function<DeathInfoLoggingItem, Map<String, WrappedPath>> mapGetter) {
		String in = StringArgumentType.getString(ct, "target").replace(" ", "");
		String name = StringArgumentType.getString(ct, "name");
		AccessingPath path = AccessingPathArgumentType.getAccessingPath(ct, "path");
		NameFilter filter = NameFilter.compile(in);
		SUBSCRIPTED_DEATH_PREDICATES.forEach((k, v) -> {
			if (filter.test(k)) {
				mapGetter.apply(v).put(name, new WrappedPath(path, name));
			}
		});
		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}
	
	private static int unsubField(CommandContext<ServerCommandSource> ct, 
			Function<DeathInfoLoggingItem, Map<String, WrappedPath>> mapGetter) {
		String in = StringArgumentType.getString(ct, "target").replace(" ", "");
		String name = StringArgumentType.getString(ct, "name");
		NameFilter filter = NameFilter.compile(in);
		SUBSCRIPTED_DEATH_PREDICATES.forEach((k, v) -> {
			if (filter.test(k)) {
				mapGetter.apply(v).remove(name);
			}
		});
		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}
	
	public static void onEntityDies(DamageSource damage, @NotNull Entity victim, float amount) {
		// TODO: Select multiple at once
		Optional<LogDeathCommand.DeathInfoLoggingItem> mayTrigger = LogDeathCommand.SUBSCRIPTED_DEATH_PREDICATES
				.values()
				.stream()
				.filter((p) -> p.test(damage, victim, amount))
				.findFirst();
		if (!mayTrigger.isPresent()) {
			return;
		}
		
		DeathInfoLoggingItem item = mayTrigger.get();
		item.increaseTriggerCount();
		if (!item.isVisible()) {
			return;
		}
		
		Text deathReport = getDeathReport(damage, victim, amount, item);
		victim.getServer()
				.getPlayerManager()
				.broadcastChatMessage(deathReport, MessageType.CHAT, Util.NIL_UUID);
	}
	
	private static Text getDeathReport(DamageSource damage, Entity victim, float amount, DeathInfoLoggingItem item) {
		MutableText t = new FormattedText("%s", "c", false, entityToString(damage.getAttacker())).asMutableText();
		t.append(new FormattedText(" + ", "rl", false).asMutableText());
		t.append(new FormattedText("%s", "r5", false, entityToString(damage.getSource())).asMutableText());
		t.append(new FormattedText("(", "rl", false).asMutableText());
		t.append(new FormattedText("%s", "ra", false, damage.name).asMutableText());
		t.append(new FormattedText(") -> ", "rl", false).asMutableText());
		t.append(new FormattedText("%s", "r7", false, entityToString(victim)).asMutableText());
		t.append(new FormattedText(" @ ", "rl", false).asMutableText());
		t.append(new FormattedText("%.2f", "r4", false, amount).asMutableText());
		if (!item.damageDetails.isEmpty()) {
			t.append(new FormattedText("cmd.logdeath.dmginf", "r6l").asMutableText());
		}
		
		for (WrappedPath l : item.damageDetails.values()) {
			t.append(new FormattedText("\n%s: ", "rcl", false, l.getName()).asMutableText());
			t.append(new FormattedText("%s", "rfo", false, l.getFrom(damage)).asMutableText());
		}

		if (!item.victimDetails.isEmpty()) {
			t.append(new FormattedText("cmd.logdeath.vctinf", "r6l").asMutableText());
		}
		
		for (WrappedPath l : item.victimDetails.values()) {
			t.append(new FormattedText("\n%s: ", "rcl", false, l.getName()).asMutableText());
			t.append(new FormattedText("%s", "rfo", false, l.getFrom(victim)).asMutableText());
		}
		
		return t;
	}
	
	private static String entityToString(Entity e) {
		if (e != null) {
			return e.getName().getString();
		} else {
			return "null";
		}
	}
	
	private static final class DeathInfoLoggingItem {
		private static final Pattern PATTERN = Pattern.compile("^(?<killer>[!a-zA-Z_]*|\\*)??"
				+ "(\\+(?<directKiller>[!a-zA-Z_]*|\\*))??"
				+ "(\\((?<cause>[!a-zA-Z_\\.]*|\\*)\\))??"
				+ "\\-\\>(?<victim>[!a-zA-Z_]*|\\*)??"
				+ "(@(?<min>[0-9\\.]+)??\\.\\.(?<max>[0-9\\.]+)??)?$");
		private static final ImmutableSet<String> DAMAGE_NAMES;
		private static final Supplier<Stream<String>> ENTITY_TYPES_PROVIDER = () -> {
			return Reflection.ENTITY_TYPE_TO_CLASS.keySet().stream()
					.map(EntityType::getId)
					.map(Identifier::getPath);
		};
		private final Predicate<DamageSource> damageValid;
		private final Predicate<Entity> victimValid;
		private final FloatPredicate amountValid;
		private final String strRep;
		final Map<String, WrappedPath> damageDetails = Maps.newTreeMap();
		final Map<String, WrappedPath> victimDetails = Maps.newTreeMap();
		private int triggerCount = 0;
		private boolean visible = true;
		
		private DeathInfoLoggingItem(Predicate<DamageSource> damageValid, Predicate<Entity> victimValid, 
				FloatPredicate amountPred, String strRep) {
			this.damageValid = damageValid;
			this.victimValid = victimValid;
			this.amountValid = amountPred;
			this.strRep = strRep;
		}
		
		public boolean isVisible() {
			return this.visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		void increaseTriggerCount() {
			this.triggerCount++;
		}
		
		int getTriggerCount() {
			return this.triggerCount;
		}

		static DeathInfoLoggingItem parse(String in) {
			// killer_type or null(cause_type)->victim_type
			// creeper, !creeper, or null, or *, or nothing should be valid
			// TODO: Wildcards & @(x0,y0,z0)->[(x1,y1,z1) | radius]
			Matcher matcher = PATTERN.matcher(in);
			if (!matcher.matches()) {
				throw new TranslatableException("cmd.logdeath.fmterr");
			}
			
			Predicate<Entity> killerPred = parseEntityPredicate(matcher.group("killer"), true);
			Predicate<Entity> directKillerPred = parseEntityPredicate(matcher.group("directKiller"), true); 
			Predicate<String> causePred = parseSourceNamePredicate(matcher.group("cause"));
			Predicate<Entity> victimPred = parseEntityPredicate(matcher.group("victim"), false);
			FloatPredicate amountPred = parseAmountPred(matcher.group("min"), matcher.group("max"));
			Predicate<DamageSource> damagePred = (ds) -> {
				return killerPred.test(ds.getAttacker())
						&& directKillerPred.test(ds.getSource())
						&& causePred.test(ds.name);
			};
			return new DeathInfoLoggingItem(damagePred, victimPred, amountPred, in);
		}
		
		private static FloatPredicate parseAmountPred(String minStr, String maxStr) {
			if (minStr != null && maxStr == null) {
				float min = Float.parseFloat(minStr);
				return (v) -> v >= min;
			} else if (minStr == null && maxStr != null) {
				float max = Float.parseFloat(maxStr);
				return (v) -> v <= max;
			} else if (minStr != null && maxStr != null) {
				float min = Float.parseFloat(minStr);
				float max = Float.parseFloat(maxStr);
				return (v) -> v <= max && v >= min;
			} else {
				return (v) -> true;
			}
		}

		private static Predicate<Entity> parseEntityPredicate(String in, boolean nullable) {
			if ("null".equals(in)) {
				if (nullable) {
					return (e) -> e == null;
				} else {
					throw new TranslatableException("cmd.logdeath.nonnull");
				}
			}
			
			if (in == null || in.isEmpty() || "*".equals(in)) {
				return (e) -> true;
			}
			
			Optional<EntityType<?>> type = EntityType.get(in.replace("!", ""));
			if (!type.isPresent()) {
				throw new TranslatableException("cmd.logdeath.noentype", in.replace("!", ""));
			}
			
			EntityType<?> t0 = type.get();
			return in.charAt(0) == '!' ? (e) -> e == null || e.getType() != t0 : (e) -> e != null && e.getType() == t0;
		}
		
		private static Predicate<String> parseSourceNamePredicate(String in) {
			if (in == null || in.isEmpty() || "*".equals(in)) {
				return (e) -> true;
			} else {
				if (in.charAt(0) != '!') {
					return (s) -> !s.equals(in.substring(1));
				} else {
					return (s) -> s.equals(in);
				}
			}
		}

		static void buildSuggestions(String in, SuggestionsBuilder sb) {
			SuggestionPhase currentPhase = SuggestionPhase.KILLER;
			int i;
			findLastDelim: {
				for (i = in.length() - 1; i >= 0; i--) {
					switch (in.charAt(i)) {
					case '>':
					case '-':
					case ')':
						currentPhase = SuggestionPhase.VICTIM;
						break findLastDelim;
					case '(':
						currentPhase = SuggestionPhase.CAUSE_NAME;
						break findLastDelim;
					case '+':
						currentPhase = SuggestionPhase.DIRECT_KILLER;
						break findLastDelim;
					}
				}
			}
			
			currentPhase.buildSuggestions(sb, i);
		}

		public boolean test(DamageSource damage, Entity victim, float amount) {
			return this.damageValid.test(damage) && this.victimValid.test(victim) && this.amountValid.test(amount);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.strRep);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			
			if (obj == null) {
				return false;
			}
			
			if (getClass() != obj.getClass()) {
				return false;
			}
			
			DeathInfoLoggingItem other = (DeathInfoLoggingItem) obj;
			return Objects.equals(this.strRep, other.strRep);
		}
		
		static {
			ImmutableSet.Builder<String> b = ImmutableSet.builder();
			for (Field f : DamageSource.class.getFields()) {
				if (f.getType() == DamageSource.class 
						&& (f.getModifiers() &  Modifier.PUBLIC) != 0 
						&& (f.getModifiers() &  Modifier.STATIC) != 0) {
					try {
						b.add(((DamageSource) f.get(null)).name);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			
			b.add("sting");
			b.add("mob");
			b.add("player");
			b.add("arrow");
			b.add("trident");
			b.add("fireworks");
			b.add("fireball");
			b.add("witherSkull");
			b.add("thrown");
			b.add("indirectMagic");
			b.add("thorns");
			b.add("explosion.player");
			b.add("explosion");
			DAMAGE_NAMES = b.build();
		}
		
		private enum SuggestionPhase {
			KILLER(ENTITY_TYPES_PROVIDER, true, "+", "->", "("), 
			DIRECT_KILLER(ENTITY_TYPES_PROVIDER, true, "->", "("), 
			CAUSE_NAME(() -> DAMAGE_NAMES.stream(), false, ")->"), 
			VICTIM(ENTITY_TYPES_PROVIDER, false, "@"), 
			DAMAGE_RANGE(() -> Stream.of("1.."), false);
			private final Supplier<Stream<String>> mainSuggestionProvider;
			private boolean suggestsNull;
			private String[] others;

			SuggestionPhase(Supplier<Stream<String>> mainSuggestionProvider, boolean suggestsNull, String ... others) {
				this.mainSuggestionProvider = mainSuggestionProvider;
				this.suggestsNull = suggestsNull;
				this.others = others;
			}
			
			void buildSuggestions(SuggestionsBuilder sb, int suggestionStart) {
				String done = sb.getRemaining().substring(0, suggestionStart + 1);
				this.mainSuggestionProvider.get().forEach((item) -> sb.suggest(done + item));
				if (this.suggestsNull) {
					sb.suggest(done + "null");
				}
				
				for (String item : this.others) {
					sb.suggest(done + item);
				}
			}
		}
	}
}
