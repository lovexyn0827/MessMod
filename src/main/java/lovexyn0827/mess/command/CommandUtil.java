package lovexyn0827.mess.command;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.i18n.I18N;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public class CommandUtil {
	public static final Predicate<ServerCommandSource> COMMAND_REQUMENT = (s) -> 
			!OptionManager.commandExecutionRequirment || s.hasPermissionLevel(1);
	public static final SuggestionProvider<ServerCommandSource> ENTITY_TYPES = (ct, b) -> {
		Registry.ENTITY_TYPE.getIds().stream().map(Identifier::getPath).forEach(b::suggest);
		return b.buildFuture();
	};
	public static final SuggestionProvider<ServerCommandSource> FIELDS_SUGGESTION = (ct, builder) -> {
		Identifier id = new Identifier(StringArgumentType.getString(ct, "entityType"));
		EntityType<?> type = Registry.ENTITY_TYPE.get(id);
		Class<?> clazz = Reflection.ENTITY_TYPE_TO_CLASS.get(type);
		Reflection.getAvailableFields(clazz).forEach(builder::suggest);
		return builder.buildFuture();
	};
	private static CommandOutput noreplyOutput;
	private static ServerCommandSource noreplySource;
	private static ServerCommandSource noreplyPlayerSource;
	private static CommandManager commandManager;
	private static boolean firstPlayerJoined = false;
	private static MinecraftServer server;
	
	public static void updateServer(MinecraftServer serverIn) {
		if(serverIn == null) {
			noreplySource = null;
			noreplyPlayerSource = null;
			commandManager = null;
			firstPlayerJoined = false;
		} else {
			 noreplyOutput = new CommandOutput(){
				public void sendSystemMessage(Text message, UUID senderUuid) {}

				public boolean shouldReceiveFeedback() {
					return false;
				}

				public boolean shouldTrackOutput() {
					return false;
				}

				public boolean shouldBroadcastConsoleToOps() {
					return false;
				}
				
			};
			noreplySource = new ServerCommandSource(noreplyOutput,
					Vec3d.ZERO, 
					Vec2f.ZERO, 
					serverIn.getOverworld(), 
					4, 
					"NOREPLY", 
					new LiteralText("NOREPLY"), 
					serverIn, 
					null);
			commandManager = serverIn.getCommandManager();
			server = serverIn;
		}
	}
	
	public static void feedback(CommandContext<? extends ServerCommandSource> ct, Object ob) {
		ct.getSource().sendFeedback(new LiteralText(ob != null ? I18N.translate(ob.toString()) : "[null]"), false);
	}
	
	public static void feedbackWithArgs(CommandContext<? extends ServerCommandSource> ct, String fmt, Object ... args) {
		ct.getSource().sendFeedback(new LiteralText(String.format(I18N.translate(fmt), args)), false);
	}
	
	public static void feedbackRaw(CommandContext<? extends ServerCommandSource> ct, Object ob) {
		ct.getSource().sendFeedback(new LiteralText(ob == null ? "[null]" : ob.toString()), false);
	}
	
	public static void error(CommandContext<? extends ServerCommandSource> ct, Object ob) {
		ct.getSource().sendError(new LiteralText(I18N.translate(ob.toString())));
	}
	
	public static void errorWithArgs(CommandContext<? extends ServerCommandSource> ct, String fmt, Object ... args) {
		ct.getSource().sendError(new LiteralText(String.format(I18N.translate(fmt), args)));
	}

	public static void error(CommandContext<ServerCommandSource> ct, String string, Exception e) {
		ct.getSource().sendError(new LiteralText(I18N.translate(string) + ": " + I18N.translate(e.getMessage()))
				.styled((s) -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(e.toString())))));
	}
	
	public static void errorRaw(CommandContext<ServerCommandSource> ct, String str, @NotNull Exception e) {
		ct.getSource().sendError(new LiteralText(str == null ? "[null]" : str)
				.styled((s) -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(e.toString())))));
	}

	public static ServerCommandSource noreplySource() {
		if(noreplySource == null) throw new RuntimeException("Called before initialzation");
		return noreplySource;
	}

	public static int execute(ServerCommandSource source, String command) {
		return commandManager.execute(source, command);
	}

	public static ServerCommandSource noreplyPlayerSources() {
		if(!firstPlayerJoined) throw new RuntimeException("Called before initialzation");
		return noreplyPlayerSource;
	}
	
	public static void tryUpdatePlayer(ServerPlayerEntity player) {
		if(firstPlayerJoined) return;
		noreplyPlayerSource = new ServerCommandSource(noreplyOutput,
				player.getPos(), 
				player.getRotationClient(), 
				server.getOverworld(), 
				4, 
				"NOREPLY", 
				new LiteralText("NOREPLY"), 
				server, 
				player);
		firstPlayerJoined = true;
	}

	public static ServerCommandSource noreplySourceFor(ServerCommandSource source) {
		Entity entity = source.getEntity();
		return new ServerCommandSource(noreplyOutput,
				source.getPosition(), 
				source.getRotation(), 
				source.getWorld(), 
				4, 
				"NOREPLY", 
				new LiteralText("NOREPLY"), 
				server, 
				entity);
	}
	
	public static SuggestionProvider<ServerCommandSource> immutableSuggestions(String ... args) {
		return (ct, builder) -> {
			Stream.of(args)
					.forEach(builder::suggest);
			return builder.buildFuture();
		};
	}
}
