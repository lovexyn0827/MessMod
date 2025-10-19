package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.RenderedBitmap;
import lovexyn0827.mess.rendering.RenderedBox;
import lovexyn0827.mess.rendering.RenderedLine;
import lovexyn0827.mess.rendering.RenderedText;
import lovexyn0827.mess.rendering.ShapeSpace;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class DrawShapeCommand {
	private static final ShapeSpace SPACE = new ShapeSpace("manual");
	private static final Set<ShapeSpace> CREATED_SPACES = new TreeSet<>(Comparator.comparing((s) -> s.name));
	private static final int DEFAULT_LIFE = 100;
	private static final int DEFAULT_COLOR = 0x31F38BFF;
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> spaceSuggestion = (ct, b) -> {
			b.suggest(SPACE.name);
			CREATED_SPACES.forEach((s) -> b.suggest(s.name));
			return b.buildFuture();
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("drawshape")
				.requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("box")
						.then(argument("corner1", Vec3ArgumentType.vec3(false))
								.then(argument("corner2", Vec3ArgumentType.vec3(false))
										.executes((ct) -> {
											return drawBox(ct, DEFAULT_COLOR, DEFAULT_LIFE, 0, SPACE);
										})
										.then(argument("color", ColorArgumentType.color())
												.executes((ct) -> {
													Formatting color = ColorArgumentType.getColor(ct, "color");
													int colorRgba = (color.getColorValue() << 8) | 0xFF;
													return drawBox(ct, colorRgba, DEFAULT_LIFE, 0, SPACE);
												})
												.then(argument("life", IntegerArgumentType.integer(0))
														.executes((ct) -> {
															Formatting color = ColorArgumentType.getColor(ct, "color");
															int colorRgba = (color.getColorValue() << 8) | 0xFF;
															int life = IntegerArgumentType.getInteger(ct, "life");
															return drawBox(ct, colorRgba, life, 0, SPACE);
														})
														.then(argument("fill", ColorArgumentType.color())
																.executes((ct) -> {
																	Formatting color = ColorArgumentType.getColor(ct, "color");
																	int colorRgba = (color.getColorValue() << 8) | 0xFF;
																	int life = IntegerArgumentType.getInteger(ct, "life");
																	Formatting fill = ColorArgumentType.getColor(ct, "fill");
																	int fillRgba = fill.getColorValue() == null ? 0 : (fill.getColorValue() << 8) | 0x5F;
																	return drawBox(ct, colorRgba, life, fillRgba, SPACE);
																})
																.then(argument("space", StringArgumentType.word())
																		.suggests(spaceSuggestion)
																		.executes((ct) -> {
																			Formatting color = ColorArgumentType.getColor(ct, "color");
																			int colorRgba = (color.getColorValue() << 8) | 0xFF;
																			int life = IntegerArgumentType.getInteger(ct, "life");
																			Formatting fill = ColorArgumentType.getColor(ct, "fill");
																			int fillRgba = fill.getColorValue() == null ? 0 : (fill.getColorValue() << 8) | 0x5F;
																			ShapeSpace space = new ShapeSpace(StringArgumentType.getString(ct, "space"));
																			CREATED_SPACES.add(space);
																			return drawBox(ct, colorRgba, life, fillRgba, space);
																		}))))))))
				.then(literal("line")
						.then(argument("corner1", Vec3ArgumentType.vec3(false))
								.then(argument("corner2", Vec3ArgumentType.vec3(false))
										.executes((ct) -> {
											return drawLine(ct, DEFAULT_COLOR, DEFAULT_LIFE, SPACE);
										})
										.then(argument("color", ColorArgumentType.color())
												.executes((ct) -> {
													Formatting color = ColorArgumentType.getColor(ct, "color");
													int colorRgba = (color.getColorValue() << 8) | 0xFF;
													return drawLine(ct, colorRgba, DEFAULT_LIFE, SPACE);
												})
												.then(argument("life", IntegerArgumentType.integer(0))
														.executes((ct) -> {
															Formatting color = ColorArgumentType.getColor(ct, "color");
															int colorRgba = (color.getColorValue() << 8) | 0xFF;
															int life = IntegerArgumentType.getInteger(ct, "life");
															return drawLine(ct, colorRgba, life, SPACE);
														})
														.then(argument("space", StringArgumentType.word())
																.suggests(spaceSuggestion)
																.executes((ct) -> {
																	Formatting color = ColorArgumentType.getColor(ct, "color");
																	int colorRgba = (color.getColorValue() << 8) | 0xFF;
																	int life = IntegerArgumentType.getInteger(ct, "life");
																	ShapeSpace space = new ShapeSpace(StringArgumentType.getString(ct, "space"));
																	CREATED_SPACES.add(space);
																	return drawLine(ct, colorRgba, life, space);
																})))))))
				.then(literal("text")
						.then(argument("pos", Vec3ArgumentType.vec3(false))
								.then(argument("text", StringArgumentType.string())
										.executes((ct) -> {
											return drawText(ct, DEFAULT_COLOR, DEFAULT_LIFE, 1.0F, SPACE);
										})
										.then(argument("color", ColorArgumentType.color())
												.executes((ct) -> {
													Formatting color = ColorArgumentType.getColor(ct, "color");
													int colorRgba = (color.getColorValue() << 8) | 0xFF;
													return drawText(ct, colorRgba, DEFAULT_LIFE, 1.0F, SPACE);
												})
												.then(argument("life", IntegerArgumentType.integer(0))
														.executes((ct) -> {
															Formatting color = ColorArgumentType.getColor(ct, "color");
															int colorRgba = (color.getColorValue() << 8) | 0xFF;
															int life = IntegerArgumentType.getInteger(ct, "life");
															return drawText(ct, colorRgba, life, 1.0F, SPACE);
														})
														.then(argument("scale", FloatArgumentType.floatArg(0))
																.executes((ct) -> {
																	Formatting color = ColorArgumentType.getColor(ct, "color");
																	int colorRgba = (color.getColorValue() << 8) | 0xFF;
																	int life = IntegerArgumentType.getInteger(ct, "life");
																	float scale = FloatArgumentType.getFloat(ct, "scale");
																	return drawText(ct, colorRgba, life, scale, SPACE);
																})
																.then(argument("space", StringArgumentType.word())
																		.suggests(spaceSuggestion)
																		.executes((ct) -> {
																			Formatting color = ColorArgumentType.getColor(ct, "color");
																			int colorRgba = (color.getColorValue() << 8) | 0xFF;
																			int life = IntegerArgumentType.getInteger(ct, "life");
																			float scale = FloatArgumentType.getFloat(ct, "scale");
																			ShapeSpace space = new ShapeSpace(StringArgumentType.getString(ct, "space"));
																			CREATED_SPACES.add(space);
																			return drawText(ct, colorRgba, life, scale, space);
																		}))))))))
				.then(literal("image")
						.then(argument("oriention", StringArgumentType.word())
								.suggests(CommandUtil.immutableSuggestions("X", "Y", "Z"))
								.then(argument("pos", Vec3ArgumentType.vec3(false))
										.then(argument("url", StringArgumentType.string())
												.executes((ct) -> {
													return drawImage(ct, DEFAULT_LIFE, 1.0F, SPACE);
												})
												.then(argument("life", IntegerArgumentType.integer(0))
														.executes((ct) -> {
															int life = IntegerArgumentType.getInteger(ct, "life");
															return drawImage(ct, life, 1.0F, SPACE);
														})
														.then(argument("scale", FloatArgumentType.floatArg(0))
																.executes((ct) -> {
																	int life = IntegerArgumentType.getInteger(ct, "life");
																	float scale = FloatArgumentType.getFloat(ct, "scale");
																	return drawImage(ct, life, scale, SPACE);
																})
																.then(argument("space", StringArgumentType.word())
																		.suggests(spaceSuggestion)
																		.executes((ct) -> {
																			int life = IntegerArgumentType.getInteger(ct, "life");
																			float scale = FloatArgumentType.getFloat(ct, "scale");
																			ShapeSpace space = new ShapeSpace(StringArgumentType.getString(ct, "space"));
																			CREATED_SPACES.add(space);
																			return drawImage(ct, life, scale, space);
																		}))))))))
				.then(literal("clear")
						.executes((ct) -> {
							MessMod.INSTANCE.shapeSender.clearSpaceFromServer(SPACE, null);
							CREATED_SPACES.forEach((s) -> MessMod.INSTANCE.shapeSender.clearSpaceFromServer(s, null));
							return Command.SINGLE_SUCCESS;
						})
						.then(argument("space", StringArgumentType.word())
								.suggests(spaceSuggestion)
								.executes((ct) -> {
									ShapeSpace space = new ShapeSpace(StringArgumentType.getString(ct, "space"));
									MessMod.INSTANCE.shapeSender.clearSpaceFromServer(space, null);
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}

	private static int drawBox(CommandContext<ServerCommandSource> ct, int color, int life, int fill, ShapeSpace space)
			throws CommandSyntaxException {
		Vec3d c1 = Vec3ArgumentType.getVec3(ct, "corner1");
		Vec3d c2 = Vec3ArgumentType.getVec3(ct, "corner2");
		RenderedBox box = new RenderedBox(new Box(c1, c2), color, fill, life, MessMod.INSTANCE.getGameTime());
		MessMod.INSTANCE.shapeSender.addShape(box, ct.getSource().getWorld().getRegistryKey(), space, null);
		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}

	private static int drawLine(CommandContext<ServerCommandSource> ct, int color, int life, ShapeSpace space) 
			throws CommandSyntaxException {
		Vec3d c1 = Vec3ArgumentType.getVec3(ct, "corner1");
		Vec3d c2 = Vec3ArgumentType.getVec3(ct, "corner2");
		RenderedLine line = new RenderedLine(c1, c2, color, life, MessMod.INSTANCE.getGameTime());
		MessMod.INSTANCE.shapeSender.addShape(line, ct.getSource().getWorld().getRegistryKey(), space, null);
		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}

	private static int drawText(CommandContext<ServerCommandSource> ct, 
			int color, int life, float scale, ShapeSpace space) 
			throws CommandSyntaxException {
		String content = StringArgumentType.getString(ct, "text");
		Vec3d pos = Vec3ArgumentType.getVec3(ct, "pos");
		RenderedText text = new RenderedText(content, pos, color, scale, life, MessMod.INSTANCE.getGameTime());
		MessMod.INSTANCE.shapeSender.addShape(text, ct.getSource().getWorld().getRegistryKey(), space, null);
		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}

	private static int drawImage(CommandContext<ServerCommandSource> ct, int life, float size, ShapeSpace space) 
			throws CommandSyntaxException {
		Vec3d pos = Vec3ArgumentType.getVec3(ct, "pos");
		String axisStr = StringArgumentType.getString(ct, "oriention");
		Direction.Axis axis = Axis.fromName(axisStr.toLowerCase());
		if (axis == null) {
			CommandUtil.errorWithArgs(ct, "cmd.general.nodef", axisStr);
			return 0;
		}
		
		String urlStr = StringArgumentType.getString(ct, "url");
		URL url;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			CommandUtil.errorWithArgs(ct, "cmd.drawshape.malurl", urlStr);
			return 0;
		}
		
		try {
			BufferedImage img = ImageIO.read(url);
			int width = img.getWidth();
			int height = img.getHeight();
			int[] pixels = new int[width * height];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					pixels[x + y * width] = rgb2Abgr(img.getRGB(x, y));
				}
			}
			
			RenderedBitmap imgToSend = new RenderedBitmap(pixels, 
					size / Math.max(height, width), height, width,  
					axis, pos, life, MessMod.INSTANCE.getGameTime());
			MessMod.INSTANCE.shapeSender.addShape(imgToSend, ct.getSource().getWorld().getRegistryKey(), space, null);
			CommandUtil.feedback(ct, "cmd.general.success");
			return Command.SINGLE_SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			CommandUtil.error(ct, "cmd.drawshape.imgfail", e);
			return 0;
		}
	}

	private static int rgb2Abgr(int rgb) {
		return (0xFF << 24)
				| ((rgb & 0x0000FF) << 16)
				| ((rgb & 0x00FF00))
				| ((rgb & 0xFF0000) >> 16);
	}
}
