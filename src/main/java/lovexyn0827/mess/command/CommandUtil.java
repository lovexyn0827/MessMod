package lovexyn0827.mess.command;

import java.util.UUID;
import java.util.stream.Stream;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class CommandUtil {
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
		ct.getSource().sendFeedback(new LiteralText(ob.toString()), false);
	}
	
	public static void error(CommandContext<? extends ServerCommandSource> ct, Object ob) {
		ct.getSource().sendError(new LiteralText(ob.toString()));
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
					.map(Object::toString)
					.forEach(builder::suggest);
			return builder.buildFuture();
		};
	}
}
