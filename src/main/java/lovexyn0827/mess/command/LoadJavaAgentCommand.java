package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import lovexyn0827.mess.util.JavaAgent;
import net.minecraft.server.command.ServerCommandSource;

public class LoadJavaAgentCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("loadjavaagent").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("fromFile")
						.then(argument("path", StringArgumentType.greedyString())
								.executes((ct) -> {
									// TODO Translate
									boolean success = false;
									Path jarPath = Paths.get(StringArgumentType.getString(ct, "path"));
									try {
										success = JavaAgent.load(jarPath).attach();
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									if (success) {
										CommandUtil.feedbackWithArgs(ct, "cmd.loadjavaagent.loadsuc", jarPath);
										return Command.SINGLE_SUCCESS;
									} else {
										CommandUtil.error(ct, "cmd.loadjavaagent.fail");
										return 0;
									}
								})))
				.then(literal("fromUrl")
						.then(argument("path", StringArgumentType.greedyString())
								.executes((ct) -> {
									// TODO Cache
									// TODO nocache
									boolean success = false;
									URL url;
									try {
										url = new URL(StringArgumentType.getString(ct, "path"));
									} catch (MalformedURLException e1) {
										CommandUtil.error(ct, "cmd.loadjavaagent.malurl");
										return 0;
									}
									
									try {
										success = JavaAgent.download(url).attach();
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									if (success) {
										CommandUtil.feedbackWithArgs(ct, "cmd.loadjavaagent.loadsuc", url);
										return Command.SINGLE_SUCCESS;
									} else {
										CommandUtil.error(ct, "cmd.loadjavaagent.fail");
										return 0;
									}
								})));
		dispatcher.register(command);
	}
}
