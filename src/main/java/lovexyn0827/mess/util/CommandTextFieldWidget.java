package lovexyn0827.mess.util;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;

import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import lovexyn0827.mess.options.EnumParser;
import lovexyn0827.mess.options.InvalidOptionException;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.options.OptionParser;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.EntitySummonArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.ParticleEffectArgumentType;
import net.minecraft.command.argument.ScoreboardCriterionArgumentType;
import net.minecraft.command.argument.ScoreboardSlotArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Lazy;

public class CommandTextFieldWidget extends TextFieldWidget {
	private final CommandDispatcher<CommandSource> commandDispatcher;
	private final boolean requireSlashBeforeCommands;
	private final Lazy<CommandSuggestor> commandSuggestor;
	
	public CommandTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text, 
			boolean requreSlash, Lazy<CommandSuggestor> commandSuggestor) {
		super(textRenderer, x, y, width, height, text);
		this.commandDispatcher = MinecraftClient.getInstance().getNetworkHandler().getCommandDispatcher();
		this.requireSlashBeforeCommands = requreSlash;
		this.commandSuggestor = commandSuggestor;
	}
	
	@Override
	protected MutableText getNarrationMessage() {
		return super.getNarrationMessage().append(this.commandSuggestor.get().getNarration());
	}
	
	@Nullable
	private ArgumentCommandNode<?, ?> getCommandNodeAtCursor() {
		@SuppressWarnings("resource")
		ClientCommandSource source = MinecraftClient.getInstance().player.networkHandler.getCommandSource();
		String text = this.getText();
		String cmd = text.startsWith("/") ? text.substring(1) : text;
		ParseResults<CommandSource> parsed = this.commandDispatcher.parse(cmd, source);
		CommandNode<CommandSource> parent;
		try {
			parent = parsed.getContext().findSuggestionContext(this.getCursor() - 1).parent;
		} catch (Exception e) {
			return null;
		}
		
		Optional<ArgumentCommandNode<?, ?>> child = parent.getChildren().stream()
				.filter((cn) -> cn instanceof ArgumentCommandNode<?, ?>)
				.findAny()
				.map((cn) -> (ArgumentCommandNode<?, ?>) cn);
		return child.orElseGet(() -> {
			return parent instanceof ArgumentCommandNode ? (ArgumentCommandNode<?, ?>) parent : null;
		});
	}
	
	@Override
	public int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
		if (this.requireSlashBeforeCommands && !this.getText().startsWith("/")) {
			return super.getWordSkipPosition(wordOffset, cursorPosition, skipOverSpaces);
		}
		
		ArgumentCommandNode<?, ?> argNode;
		CursorMode delimDetector = OptionManager.smartCursorMode;
		if (delimDetector.shouldParseCommand) {
			argNode = this.getCommandNodeAtCursor();
			if (argNode == null) {
				return super.getWordSkipPosition(wordOffset, cursorPosition, skipOverSpaces);
			}
		} else {
			argNode = null;
		}
		
		// Vanilla version (modified)
		int i = cursorPosition;
		boolean reverse = wordOffset < 0;
		int wordsToSkip = Math.abs(wordOffset);
		for (int k = 0; k < wordsToSkip; ++k) {
			if (reverse) {
				// Skip over initial spaces
				while (skipOverSpaces && i > 0 
						 && delimDetector.isWordDelimiter(argNode, this.getText().charAt(i - 1), i - 1)) {
					--i;
				}
				
				// Find next word delimiter
				while (i > 0 && !delimDetector.isWordDelimiter(argNode, this.getText().charAt(i - 1), i - 1)) {
					--i;
				}
				
				continue;
			} else {
				// Skip one word
				int len = this.getText().length();
				while (i < len && !delimDetector.isWordDelimiter(argNode, this.getText().charAt(i), i)) {
					i++;
				}
				
				// Skip tailing spaces
				while (skipOverSpaces && i < len
						 && delimDetector.isWordDelimiter(argNode, this.getText().charAt(i), i)) {
					++i;
				}
			}
		}
		
		return i;
	}
	
	public static enum CursorMode {
		VANILLA(false) {
			@Override
			boolean isWordDelimiter(ArgumentCommandNode<?, ?> node, char ch, int pos) {
				return ch == ' ';
			}
		}, 
		NON_LITERAL(false) {
			@Override
			boolean isWordDelimiter(ArgumentCommandNode<?, ?> node, char ch, int pos) {
				return !(ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= 9 
						|| ch == '_' || ch == '$');
			}
		}, 
		NERDY(false) {
			@Override
			boolean isWordDelimiter(ArgumentCommandNode<?, ?> node, char ch, int pos) {
				return ch == ' ' || ch == '=' || ch == '.' || ch == ',' || ch == '[' || ch == ']' || ch == '(' 
						|| ch == ')' || ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '#' || ch == ':';
			}
		},
		NORMAL(true) {
			// TODO make it ACTUALLY smart
			@Override
			boolean isWordDelimiter(ArgumentCommandNode<?, ?> node, char ch, int pos) {
				if (!(node instanceof ArgumentCommandNode)) {
					return VANILLA.isWordDelimiter(node, ch, pos);
				}
				
				ArgumentType<?> argType = ((ArgumentCommandNode<?, ?>) node).getType();
				if (argType instanceof EntityArgumentType) {
					// Entity selector
					return ch == ' ' || ch == '[' || ch == ',' || ch == '=' || ch == ':' || ch == ']';
				} else if (argType instanceof DimensionArgumentType || argType instanceof EntitySummonArgumentType 
						|| argType instanceof IdentifierArgumentType 
						|| argType instanceof ParticleEffectArgumentType) {
					// Identifier
					return ch == ' ' || ch == ':' || ch == '/';
				} else if (argType instanceof ItemPredicateArgumentType || argType instanceof ItemStackArgumentType
						|| argType instanceof NbtCompoundArgumentType || argType instanceof NbtElementArgumentType
						|| argType instanceof TextArgumentType || argType instanceof BlockStateArgumentType) {
					// NBT-like
					return ch == ' ' || ch == '{' || ch == '=' || ch == '}' || ch == ',' || ch == '[' || ch == ':'
							|| ch == ']';
				} else if (argType instanceof ItemSlotArgumentType || argType instanceof ScoreboardSlotArgumentType) {
					// container.36
					return ch == ' ' || ch == '.';
				} else if (argType instanceof NbtPathArgumentType) {
					// NBT path
					return ch == ' ' || ch == '.' || ch == '[' || ch == ']';
				} else if (argType instanceof ScoreboardCriterionArgumentType) {
					// mc:a.b.c
					return ch == ' ' || ch == ':' || ch == '.';
				} else if (argType instanceof AccessingPathArgumentType) {
					return ch == ' ' || ch == '.' || ch == ',' || ch == '(' || ch == '!' || ch == '/' || ch == '#'
							|| ch == '[' || ch == '<' || ch == ':' || ch == ')' || ch == ']' || ch == '>';
				} else {
					return VANILLA.isWordDelimiter(node, ch, pos);
				}
			}
		},  
		GREEDY(true) {
			@Override
			boolean isWordDelimiter(ArgumentCommandNode<?, ?> node, char ch, int pos) {
				return ch >= 'A' && ch <= 'Z' || NORMAL.isWordDelimiter(node, ch, pos);
			}
		}, 
		CUSTOM(false) {
			@Override
			boolean isWordDelimiter(ArgumentCommandNode<?, ?> node, char ch, int pos) {
				return OptionManager.smartCursorCustomWordDelimiters.contains(ch);
			}
		};
		
		abstract boolean isWordDelimiter(ArgumentCommandNode<?, ?> argNode, char ch, int pos);
		
		protected final boolean shouldParseCommand;
		
		private CursorMode(boolean shouldParseCommand) {
			this.shouldParseCommand = shouldParseCommand;
		}
		
		public static class Parser extends EnumParser<CursorMode> {
			public Parser() {
				super(CursorMode.class);
			}
		}
	}
	
	public static class CharSetParser implements OptionParser<CharSet> {
		@Override
		public CharSet tryParse(String str) throws InvalidOptionException {
			CharSet set = new CharOpenHashSet();
			for (char c : str.toCharArray()) {
				set.add(c);
			}
			
			return set;
		}

		@Override
		public String serialize(CharSet val) {
			StringBuilder sb = new StringBuilder();
			val.forEach((int c) -> sb.append((char) c));
			return sb.toString();
		}
	}
}
