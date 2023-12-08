package lovexyn0827.mess.util.access;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.TranslatableException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;

public final class AccessingPathArgumentType implements ArgumentType<AccessingPath> {
	private final Function<CommandContext<ServerCommandSource>, Type> inputTypeGetter;
	
	private AccessingPathArgumentType() {
		this.inputTypeGetter = null;
	}
	
	private AccessingPathArgumentType(Function<CommandContext<ServerCommandSource>, Type> inputTypeGetter) {
		this.inputTypeGetter = inputTypeGetter;
	}

	public static AccessingPathArgumentType accessingPathArg() {
		return new AccessingPathArgumentType();
	}
	
	public static AccessingPathArgumentType accessingPathArg(
			Function<CommandContext<ServerCommandSource>, Type> inputTypeGetter) {
		return new AccessingPathArgumentType(inputTypeGetter);
	}
	
	public static AccessingPathArgumentType accessingPathArg(Class<?> out) {
		return new AccessingPathArgumentType((ct) -> out);
	}
	
	public static AccessingPath getAccessingPath(CommandContext<ServerCommandSource> ct, String string) {
		return ct.getArgument(string, AccessingPath.class);
	}
	
	/**
	 * Parse a string representation of {@link AccessingPath} into an instance.
	 * Example: !passengers.[0].getPos().y
	 * @throws CommandSyntaxException If the input is not a valid representation of an accessing path.
	 */
	@Override
	public AccessingPath parse(StringReader reader) throws CommandSyntaxException {
		LinkedList<Node> nodes = Lists.newLinkedList();
		String stringRepresentation = reader.getRemaining().trim();
		reader.setCursor(reader.getTotalLength());
		if(!stringRepresentation.endsWith(".")) {
			stringRepresentation += '.';
		}
		
		StringReader sr = new StringReader(stringRepresentation);
		Node n;
		int i = 0;
		while((n = readNode(sr)) != null) {
			if(!nodes.isEmpty() && !n.canFollow(nodes.getLast()) && OptionManager.strictAccessingPathParsing) {
				throw new TranslatableException("Node %s (%s) couldn't follow %s (%s)", 
						n.getClass().getSimpleName(), n, 
						nodes.getLast().getClass().getSimpleName(), nodes.getLast());
			}
			
			n.ordinary = i;
			nodes.add(n);
		}
		
		return new JavaAccessingPath(nodes, stringRepresentation);
	}

	@Nullable
	private Node readNode(StringReader sr) throws CommandSyntaxException {
		if(!sr.canRead()) {
			return null;
		}
		
		String nodeStr;
		switch(sr.peek()) {
		case '!':
			sr.skip();
			// This is fine since dots are invalid in field names.
			nodeStr = sr.readStringUntil('.');
			return new FieldNode(nodeStr);
		case '[':
			//Element
			sr.skip();
			// This is fine as only numbers are allowed here.
			nodeStr = sr.readStringUntil(']');
			sr.skip();
			try {
				return new ElementNode(Integer.parseInt(nodeStr));
			} catch (NumberFormatException e) {
				throw new TranslatableException("exp.reqint", nodeStr);
			}
		case '<':
			//Map
			nodeStr = readWrapped(sr, '<', '>');
			sr.skip();
			return new ValueOfMapNode(Literal.parse(nodeStr));
		case '>':
			sr.skip();
			nodeStr = readUntil(sr, '.');
			return new MapperNode(nodeStr);
		case '(':
			sr.skip();
			nodeStr = sr.readStringUntil(')');
			sr.skip();
			return new ClassCastNode(nodeStr);
		default:
			nodeStr = readUntil(sr, '.');
			Matcher matcher = MethodNode.METHOD_PATTERN.matcher(nodeStr);
			if(matcher.matches()) {
				return new MethodNode(matcher.group("name"), matcher.group("types"), matcher.group("args"));
			} else {
				switch(nodeStr) {
				case "x" : 
					return new ComponentNode.X();
				case "y" : 
					return new ComponentNode.Y();
				case "z" : 
					return new ComponentNode.Z();
				case "size" : 
					return new SizeNode();
				default : 
					Node node;
					if((node = SimpleNode.byName(nodeStr)) != null) {
						return node;
					} else {
						node = CustomNode.byName(nodeStr);
						if(node != null) {
							return node;
						} else {
							throw new TranslatableException("exp.unknownnode", nodeStr);
						}
					}
				}
			}
		}
	}
	
	private static String readWrapped(StringReader sr, char openCh, char closeCh) throws CommandSyntaxException {
		int depth = 0;
		int start = sr.getCursor();
		int end = -1;
		while(sr.canRead()) {
			char ch = sr.read();
			if(ch == '\\') {
				sr.skip();
				continue;
			}
			
			if(ch == openCh) {
				depth++;
				continue;
			}
			
			if(ch == closeCh) {
				if(--depth == 0) {
					end = sr.getCursor();
					break;
				}
				
				continue;
			}
		}
		
		if(end == -1) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
					.readerExpectedEndOfQuote()
					.createWithContext(sr);
		} else {
			return sr.getString().substring(start + 1, end - 1);
		}
	}
	
	private static String readUntil(StringReader sr, char endCh) throws CommandSyntaxException {
		int start = sr.getCursor();
		int end = -1;
		while(sr.canRead()) {
			char ch = sr.read();
			if(ch == '\\') {
				sr.skip();
				continue;
			}
			
			if(ch == endCh) {
				end = sr.getCursor();
				break;
			}
		}
		
		if(end == -1) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedEndOfQuote().createWithContext(sr);
		} else {
			return sr.getString().substring(start, end - 1);
		}
	}
	
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ct, 
			SuggestionsBuilder unoffsetedBuilder) {
		String in = unoffsetedBuilder.getRemaining();
    	int len = in.length();
    	int lastNotEscapedDot = -1;	// Tricky
    	for(int i = 0; i < len; i++) {
    		char c = in.charAt(i);
    		if(c == '\\') {
    			i++;
    		} else if(c == '.') {
    			lastNotEscapedDot = i;
    		}
    	}

    	
    	int offset = unoffsetedBuilder.getStart() + lastNotEscapedDot + 1;
    	SuggestionsBuilder builder = unoffsetedBuilder.createOffset(offset);
        if(lastNotEscapedDot == len -1) {
        	// The last node is empty
        	CustomNode.listSuggestions(builder);
        	SimpleNode.appendSuggestions(builder);
        	return builder
            		.suggest(">")
            		.suggest("!")
            		.suggest("<")
            		.suggest("[")
            		.suggest("(")
            		.suggest("x")
            		.suggest("y")
            		.suggest("z")
            		.suggest("size")
            		.buildFuture();
        } else {
        	String lastNodeStr = in.substring(lastNotEscapedDot + 1);
        	switch(lastNodeStr.charAt(0)) {
        	case '>':
        		return builder
        				.suggest(">java/lang/")
        				.suggest(">java/util/")
        				.suggest(">net/minecraft/")
        				.suggest(">net/minecraft/block")
        				.suggest(">net/minecraft/entity")
        				.suggest(">net/minecraft/world")
        				.suggest(">net/minecraft/util")
        				.suggest(lastNodeStr + "::")
        				.buildFuture();
        	default:
        		if(OptionManager.accessingPathDynamicAutoCompletion) {
        			if(ct.getSource() instanceof ServerCommandSource 
        					&& this.inputTypeGetter != null) {
        				// Server side
        				@SuppressWarnings("unchecked")
    					Type inType = this.inputTypeGetter.apply((CommandContext<ServerCommandSource>) ct);
            			String prefix;
            			AccessingPath completed;
            			String completedStr = in.substring(0, lastNotEscapedDot + 1);
            			try {
    						completed = completedStr.isEmpty() ? 
    								AccessingPath.DUMMY : this.parse(new StringReader(completedStr));
    					} catch (CommandSyntaxException e) {
    						completed = AccessingPath.DUMMY;
    					}
            			
            			if(completed instanceof JavaAccessingPath) {
            				try {
    							((JavaAccessingPath) completed).initialize(inType);
    						} catch (AccessingFailureException e) {
    							return builder.buildFuture();
    						}
            			}
            			
            			if(lastNodeStr.charAt(0) == '!') {
            				prefix = lastNodeStr.substring(1);
            				Reflection.getAvailableFieldNames(Reflection.getRawType(completed.getOutputType()))
        							.stream()
        							.filter((fn) -> fn.contains(prefix) || prefix.isEmpty())
        							.forEach((fn) -> builder.suggest("!" + fn));
            				builder.suggest(".");
            			} else {
            				prefix = lastNodeStr;
            				MutableBoolean anyExactlyMatching = new MutableBoolean(false);
            				Reflection.getAllMethods(Reflection.getRawType(completed.getOutputType()))
        							.stream()
        							.filter((m) -> m.getName().contains(prefix))
        							.map((m) -> {
        								return MessMod.INSTANCE
        										.getMapping()
        										.namedMethod(m.getName(), org.objectweb.asm.Type.getMethodDescriptor(m));
        							})
        							.distinct()
        							.forEach((mn) -> {
        								builder.suggest(mn);
        								if(mn.equals(prefix)) {
        									anyExactlyMatching.setTrue();
        								}
        							});
            				if(anyExactlyMatching.booleanValue()) {
                				builder.suggest("<")
                						.suggest("(");
            				}
            			}
        			} else {
        				// Client side
        				try {
        					@SuppressWarnings("unchecked")
							CommandContext<CommandSource> clientCt = (CommandContext<CommandSource>) ct;
							return SuggestionProviders.ASK_SERVER.getSuggestions(clientCt, builder);
						} catch (CommandSyntaxException e) {
							MessMod.LOGGER.error("Unable to ask the server for suggestions!");
							e.printStackTrace();
						}
        			}
        		}
        	}

			return builder.buildFuture();
        }
    }

	public static void registerArgumentType() {
		ArgumentTypes.register("mess_accessing_path", AccessingPathArgumentType.class, 
				new ConstantArgumentSerializer<AccessingPathArgumentType>(AccessingPathArgumentType::accessingPathArg));
	}
}
